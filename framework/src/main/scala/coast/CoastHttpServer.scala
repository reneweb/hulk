package coast

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethod, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import coast.config.{AcceptHeaderVersioning, AcceptVersionHeaderVersioning, CoastConfig, PathVersioning}
import coast.http.CoastHttpResponse._
import coast.http.request.HttpRequestBody._
import coast.http.{Action, CoastHttpRequest, CoastHttpResponse, NotFound}
import coast.routing.{Filter, Filters, Router}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
  * Created by reweber on 18/12/2015
  */
class CoastHttpServer(router: Router, coastConfig: Option[CoastConfig])
                     (implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) {

  val interface = coastConfig.flatMap(_.interface).getOrElse("localhost")
  val port = coastConfig.flatMap(_.port).getOrElse(10000)
  val serverSettingsOpt = coastConfig.flatMap(_.serverSettings)
  val parallelism = coastConfig.flatMap(_.asyncParallelism).getOrElse(5)

  def run() = {
    router match {
      case routerWithFilters: Filters => buildHttpServer(router, routerWithFilters)
      case _ => buildHttpServer(router, new Filters { def filters = {Seq()} })
    }
  }

  private def buildHttpServer(router: Router, filters: Filters) = {

    val filtersSeq = filters.filters
    val routesWithRegex = router.router.map{ case (routeDef, action) =>
      val pathVarNames = ":\\{[^}]*\\}".r.findAllIn(routeDef.path).toList.map(_.drop(2).dropRight(1))
      val routeWithRegex = ":\\{[^}]*\\}".r.replaceAllIn(routeDef.path, r => {
        r.toString().replace(":{", "(?<").replace("}", ">[^/]+)")
      })

      (RouteDefWithRegex(routeDef.method, routeWithRegex, pathVarNames), action)
    }

    val flow: Flow[HttpRequest, HttpResponse, Any] = Flow[HttpRequest]
      .mapAsync(parallelism) { implicit request =>

        val versionOpt: Option[String] = findVersion(request)
        val preparedRequest = removeVersionFromRequestIfPathVersioned(request, versionOpt)

        val matchedRoute = matchRequestToRoute(routesWithRegex, preparedRequest)
        val pathVarMap = extractPathVariables(matchedRoute, request)

        val coastHttpRequest = CoastHttpRequest(request.method, request.uri.path.toString(), request.headers, request.entity)(pathVarMap,
          request.uri.query(), request.uri.fragment)(request.cookies)

        val outgoingFilterResults = executeOutgoingFilters(filtersSeq, coastHttpRequest)
        val incomingFilterResultOpt = findIncomingFilter(filtersSeq, outgoingFilterResults.size)

        val runIncomingFilterWithRequest = runIncomingFilter(coastHttpRequest) _
        val response = incomingFilterResultOpt.map(runIncomingFilterWithRequest)
          .getOrElse(runActionIfMatch(versionOpt, matchedRoute, coastHttpRequest))

        outgoingFilterResults
          .foldLeft(response){ case (resp, filterResult) => resp.flatMap(filterResult) }
          .map(toAkkaHttpResponse => toAkkaHttpResponse)
      }

    serverSettingsOpt.map { serverSettings =>
      Http().bindAndHandle(flow, interface, port, serverSettings)
    }.getOrElse {
      Http().bindAndHandle(flow, interface, port)
    }
  }

  def runActionIfMatch(versionOpt: Option[String], matchedRoute: Option[(RouteDefWithRegex, Action)], coastHttpRequest: CoastHttpRequest): Future[CoastHttpResponse] = {
    matchedRoute.map { routeWithAction =>
      runAction(versionOpt, coastHttpRequest, routeWithAction)
    }.getOrElse(Action { req => NotFound() }.run(coastHttpRequest).get)
  }

  def runAction(versionOpt: Option[String], coastHttpRequest: CoastHttpRequest, routeWithAction: (RouteDefWithRegex, Action)): Future[CoastHttpResponse] = {
    versionOpt.map { version =>
      routeWithAction._2.run(version, coastHttpRequest).getOrElse(Future(NotFound()))
    }.getOrElse {
      routeWithAction._2.run(coastHttpRequest).getOrElse(Future(NotFound()))
    }
  }

  def removeVersionFromRequestIfPathVersioned(request: HttpRequest, versionOpt: Option[String]): HttpRequest = {
    coastConfig.flatMap { config =>
      config.versioning.map { c => c match {
        case p: PathVersioning => request.copy(uri = request.uri.copy(path = request.uri.path.dropChars(versionOpt.get.length + 1)))
        case _ => request
      }}
    }.getOrElse(request)
  }

  def findVersion(request: HttpRequest): Option[String] = {
    coastConfig.flatMap { config =>
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

  private def runIncomingFilter(coastHttpRequest: CoastHttpRequest)(filter: Filter) = {
    filter.filter(r => Future(r))(coastHttpRequest).result.swap.toOption.get
  }

  private def executeOutgoingFilters(filters: Seq[Filter], coastHttpRequest: CoastHttpRequest) = {
    def rec(filters: Seq[Filter], coastRequest: CoastHttpRequest): Seq[CoastHttpResponse => Future[CoastHttpResponse]] =
      filters.headOption.map(f => {
        val filterResult = f.filter(r => Future(r))(coastRequest)

        if(filterResult.result.isRight) {
          val returnSeq = rec(filters.tail, coastRequest)
          returnSeq :+ filterResult.result.toOption.get
        } else {
          Seq.empty
        }
      }).getOrElse(Seq.empty)

    rec(filters, coastHttpRequest).reverse
  }
}

object CoastHttpServer {

  def apply(router: Router, coastConfig: Option[CoastConfig], actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) = {
    new CoastHttpServer(router, coastConfig)(actorSystem, actorMaterializer)
  }

  def apply(router: Router, coastConfig: Option[CoastConfig] = None) = {
    implicit val actorSystem = ActorSystem()
    implicit val actorMaterializer = ActorMaterializer()(actorSystem)

    new CoastHttpServer(router, coastConfig)
  }
}

case class RouteDefWithRegex(method: Option[HttpMethod], path: String, pathVarNames: Seq[String])