package hulk

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import cats.data.Xor
import com.codahale.metrics.MetricRegistry
import hulk.config.HulkConfig
import hulk.filtering.Filters
import hulk.http.HulkHttpResponse
import hulk.routing.Router
import hulk.server.{RequestHandler, RouteRegexGenerator}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
    val routesWithRegex = new RouteRegexGenerator(router).generateRoutesWithRegex()
    val requestHandler = new RequestHandler(router, routesWithRegex, filtersSeq, hulkConfig)

    val flow: Flow[HttpRequest, HttpResponse, Any] = Flow[HttpRequest]
      .mapAsync(parallelism) { implicit request =>
        val timerRequestsByMethodAndUri = metricRegistry.timer(s"hulk.request.method.${request.method.name}.uri.${request.uri.toString()}").time()
        val timerRequests = metricRegistry.timer(s"hulk.request").time()
        metricRegistry.meter(s"hulk.request.useragent.${request.headers.find(_.is("user-agent")).getOrElse("unknown")}").mark()
        logger.debug(s"Request: ${request.method} ${request.uri.toString()}")
        logger.trace(s"Request headers: ${request.headers.mkString(", ")}")

        val rateLimitResultFuture: Future[Xor[HttpResponse, HttpRequest]] = requestHandler.executeRateLimiting(request)
        val responseFuture = rateLimitResultFuture.flatMap(_.map(requestHandler.handleRequest).leftMap(Future(_)).merge)

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
      Http().bindAndHandle(flow, interface, port, settings = serverSettings)
    }.getOrElse {
      Http().bindAndHandle(flow, interface, port)
    }
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