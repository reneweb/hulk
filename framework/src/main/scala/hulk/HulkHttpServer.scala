package hulk

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethod, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.codahale.metrics.MetricRegistry
import hulk.config.HulkConfig
import hulk.config.versioning.{AcceptVersionHeaderVersioning, AcceptHeaderVersioning, PathVersioning}
import hulk.ratelimiting.GlobalRateLimiting
import hulk.http._
import hulk.http.request.HttpRequestBody._
import hulk.routing.{Filter, Filters, Router}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
  * Created by reweber on 18/12/2015
  */
class HulkHttpServer(router: Router, hulkConfig: Option[HulkConfig])
                    (implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) {

  private val logger = LoggerFactory.getLogger(classOf[HulkHttpServer])

  private val interface = hulkConfig.flatMap(_.interface).getOrElse("localhost")
  private val port = hulkConfig.flatMap(_.port).getOrElse(10000)
  private val serverSettingsOpt = hulkConfig.flatMap(_.serverSettings)
  private val parallelism = hulkConfig.flatMap(_.asyncParallelism).getOrElse(5)

  private val metricRegistry = hulkConfig.flatMap(_.metricRegistry).getOrElse(new MetricRegistry)

  def run() = {
    val server =
      router match {
        case routerWithFilters: Filters => buildHttpServer(router, routerWithFilters)
        case _ => buildHttpServer(router, new Filters { def filters = {Seq()} })
      }

    server.foreach {server =>
      logger.info("""    __  ____  ____    __ __""")
      logger.info("""   / / / / / / / /   / //_/""")
      logger.info("""  / /_/ / / / / /   / ,<   """)
      logger.info(""" / __  / /_/ / /___/ /| |  """)
      logger.info("""/_/ /_/\____/_____/_/ |_|  """)
      logger.info("")
      logger.info(s"Host: ${server.localAddress.getHostName} - Port: ${server.localAddress.getPort}")
    }

    server
  }

  private def buildHttpServer(router: Router, filters: Filters) = {

    val filtersSeq = filters.filters
    val routesWithRegex = router.router.map{ case (routeDef, action) =>
      val pathVarNames = ":\\{[^}]*\\}".r.findAllIn(routeDef.path).toList.map(_.drop(2).dropRight(1)).map(_.split(":")(0).trim)
      val routeWithRegex = ":\\{[^}]*\\}".r.replaceAllIn(routeDef.path, r => {
        val name = r.toString().drop(2).dropRight(1).split(":")(0)
        val actualRegex = r.toString().drop(2).dropRight(1).split(":").lift(1).map(_.trim).getOrElse("[^/]+")
        s"(?<$name>$actualRegex)"
      })

      (RouteDefWithRegex(routeDef.method, routeWithRegex, pathVarNames), action)
    }

    val flow: Flow[HttpRequest, HttpResponse, Any] = Flow[HttpRequest]
      .mapAsync(parallelism) { implicit request =>
        def doFilteringAndRouting(): Future[HttpResponse] = {
          val versionOpt: Option[String] = findVersion(request)
          val preparedRequest = removeVersionFromRequestIfPathVersioned(request, versionOpt)

          val matchedRoute = matchRequestToRoute(routesWithRegex, preparedRequest)
          val pathVarMap = extractPathVariables(matchedRoute, request)

          val hulkHttpRequest = HulkHttpRequest(request.method, request.uri.path.toString(), request.headers, request.entity)(pathVarMap,
            request.uri.query(), request.uri.fragment)(request.cookies)

          val outgoingFilterResults = executeOutgoingFilters(filtersSeq, hulkHttpRequest)
          val incomingFilterResultOpt = findIncomingFilter(filtersSeq, outgoingFilterResults.size)

          val runIncomingFilterWithRequest = runIncomingFilter(hulkHttpRequest) _
          val response = incomingFilterResultOpt.map(runIncomingFilterWithRequest)
            .getOrElse(runActionIfMatch(versionOpt, matchedRoute, hulkHttpRequest))

          outgoingFilterResults
            .foldLeft(response) { case (resp, filterResult) => resp.flatMap(filterResult) }
            .map(toAkkaHttpResponse => toAkkaHttpResponse)
        }

        val timerRequestsByMethodAndUri = metricRegistry.timer(s"hulk.request.method.${request.method.name}.uri.${request.uri.toString()}").time()
        val timerRequests = metricRegistry.timer(s"hulk.request").time()
        metricRegistry.meter(s"hulk.request.useragent.${request.headers.find(_.is("user-agent")).getOrElse("unknown")}").mark()
        logger.debug(s"Request: ${request.method} ${request.uri.toString()}")
        logger.trace(s"Request headers: ${request.headers.mkString(", ")}")

        val responseFuture: Future[HttpResponse] =
          router match {
            case rateLimiting: GlobalRateLimiting =>
              rateLimiting.rateLimiter.limitExceeded(request.headers, request.cookies).flatMap { limitExceeded =>
                if(limitExceeded) Future(TooManyRequests()) else doFilteringAndRouting()
              }
            case _ => doFilteringAndRouting()
          }

        responseFuture.onComplete { c =>
          timerRequestsByMethodAndUri.stop()
          timerRequests.stop()
        }
        responseFuture.foreach { r =>
          metricRegistry.meter(s"hulk.response.status.${r.status.intValue()}").mark()
          logger.debug(s"Response: ${r.protocol.value} ${r.status.value} ")
        }

        responseFuture
      }

    serverSettingsOpt.map { serverSettings =>
      Http().bindAndHandle(flow, interface, port, serverSettings)
    }.getOrElse {
      Http().bindAndHandle(flow, interface, port)
    }
  }

  def runActionIfMatch(versionOpt: Option[String], matchedRoute: Option[(RouteDefWithRegex, Action)], httpRequest: HulkHttpRequest): Future[HulkHttpResponse] = {
    matchedRoute.map { routeWithAction =>
      runAction(versionOpt, httpRequest, routeWithAction)
    }.getOrElse(Action { req => NotFound() }.run(httpRequest).get)
  }

  def runAction(versionOpt: Option[String], httpRequest: HulkHttpRequest, routeWithAction: (RouteDefWithRegex, Action)): Future[HulkHttpResponse] = {
    versionOpt.map { version =>
      routeWithAction._2.run(version, httpRequest).getOrElse(Future(NotFound()))
    }.getOrElse {
      routeWithAction._2.run(httpRequest).getOrElse(Future(NotFound()))
    }
  }

  def removeVersionFromRequestIfPathVersioned(request: HttpRequest, versionOpt: Option[String]): HttpRequest = {
    hulkConfig.flatMap { config =>
      config.versioning.map { c => c match {
        case p: PathVersioning => request.copy(uri = request.uri.copy(path = request.uri.path.dropChars(versionOpt.get.length + 1)))
        case _ => request
      }}
    }.getOrElse(request)
  }

  def findVersion(request: HttpRequest): Option[String] = {
    hulkConfig.flatMap { config =>
      config.versioning.flatMap { c => c match {
        case p: PathVersioning => Some(request.uri.path.toString().drop(1).takeWhile(_ != '/'))
        case a: AcceptHeaderVersioning => request.headers.find(_.name() == "Accept").flatMap(h => a.versionRegex.r.findFirstIn(h.value()))
        case av: AcceptVersionHeaderVersioning => request.headers.find(_.name() == "AcceptVersion").map(_.value())
        case _ => throw new IllegalArgumentException("Not a valid versioning strategy")
      }}
    }
  }

  def extractPathVariables(matchedRoute: Option[(RouteDefWithRegex, Action)], request: HttpRequest): Map[String, String] = {
    matchedRoute.map { case (routeDef, action) =>
      routeDef.pathVarNames.map { pathVarName =>
        Try(routeDef.path.r(routeDef.pathVarNames: _*).findFirstMatchIn(request.uri.path.toString()).get.group(pathVarName))
          .map(m => Some(pathVarName -> m))
          .getOrElse(None)
      }.filter(_.isDefined).map(_.get).toMap
    }.getOrElse(Map.empty)
  }

  private def matchRequestToRoute(routes: Map[RouteDefWithRegex, Action], request: HttpRequest): Option[(RouteDefWithRegex, Action)] = {
    def matchAnyMethod(routeDef: RouteDefWithRegex) = routeDef.method.isEmpty
    def matchAnyUri(routeDef: RouteDefWithRegex) = routeDef.path.isEmpty

    val method = request.method
    val path = request.uri.path

    val routesWithMatchingMethod = routes.filter { case (routeDef, _) => routeDef.method.contains(method) || matchAnyMethod(routeDef) }

    routesWithMatchingMethod.find { case (routeDef, action) =>
      path.toString().matches(routeDef.path)
    }.map { case (routeDef, action) =>
      (routeDef, action)
    }
  }

  private def findIncomingFilter(filtersSeq: Seq[Filter], outgoingFilterResultsSize: Int): Option[Filter] =
    filtersSeq.lift(outgoingFilterResultsSize)

  private def runIncomingFilter(httpRequest: HulkHttpRequest)(filter: Filter) = {
    filter.filter(r => Future(r))(httpRequest).result.swap.toOption.get
  }

  private def executeOutgoingFilters(filters: Seq[Filter], httpRequest: HulkHttpRequest) = {
    def rec(filters: Seq[Filter], httpRequest: HulkHttpRequest): Seq[HulkHttpResponse => Future[HulkHttpResponse]] =
      filters.headOption.map(f => {
        val filterResult = f.filter(r => Future(r))(httpRequest)

        if(filterResult.result.isRight) {
          val returnSeq = rec(filters.tail, httpRequest)
          returnSeq :+ filterResult.result.toOption.get
        } else {
          Seq.empty
        }
      }).getOrElse(Seq.empty)

    rec(filters, httpRequest).reverse
  }
}

object HulkHttpServer {

  def apply(router: Router, hulkConfig: Option[HulkConfig], actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) = {
    new HulkHttpServer(router, hulkConfig)(actorSystem, actorMaterializer)
  }

  def apply(router: Router, hulkConfig: Option[HulkConfig] = None) = {
    implicit val actorSystem = ActorSystem()
    implicit val actorMaterializer = ActorMaterializer()(actorSystem)

    new HulkHttpServer(router, hulkConfig)
  }
}

case class RouteDefWithRegex(method: Option[HttpMethod], path: String, pathVarNames: Seq[String])