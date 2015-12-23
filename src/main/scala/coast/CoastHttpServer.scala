package coast

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import coast.config.CoastConfig
import coast.http.CoastHttpRequest
import coast.http.CoastHttpRequest._
import coast.http.CoastHttpResponse._
import coast.http.RoutingHttpRequest._
import coast.routing.{Filters, Router}

import scala.concurrent.ExecutionContext.Implicits.global

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

    val filterFunc = reduceFiltersIntoOneFunc(filters)

    val flow: Flow[HttpRequest, HttpResponse, Any] = Flow[HttpRequest]
      .map(filterFunc(_))
      .filter(_.isDefined)
      .map(_.get)
      .map(request => (request, router.router(request)))
      .mapAsync(parallelism){ case (request, action) => action.run(request).map(chr => chr) }

    serverSettingsOpt.map { serverSettings =>
      Http().bindAndHandle(flow, interface, port, serverSettings)
    }.getOrElse {
      Http().bindAndHandle(flow, interface, port)
    }
  }

  private def reduceFiltersIntoOneFunc(filters: Filters): (CoastHttpRequest => Option[CoastHttpRequest]) = {
    if(filters.filters.isEmpty) {
      req => Some(req)
    } else {
      filters.filters.map(_.filter).reduce[CoastHttpRequest => Option[CoastHttpRequest]] {
        case (filterF, filterOtherF) => filterF andThen (resOpt => resOpt.flatMap(filterOtherF))
      }
    }
  }
}

object CoastHttpServer {

  def apply(router: Router, coastConfig: Option[CoastConfig], actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) = {
    new CoastHttpServer(router, coastConfig)(actorSystem, actorMaterializer)
  }

  def apply(router: Router, coastConfig: Option[CoastConfig]) = {
    implicit val actorSystem = ActorSystem()
    implicit val actorMaterializer = ActorMaterializer()(actorSystem)

    new CoastHttpServer(router, coastConfig)
  }
}