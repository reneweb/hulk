package coast

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import coast.config.CoastConfig
import coast.http.CoastHttpRequest
import coast.http.RoutingHttpRequest._
import coast.http.CoastHttpRequest._
import coast.http.CoastHttpResponse._
import coast.routing.{Filter, Filters, Router}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 18/12/2015
  */
case class CoastHttpServer(router: Router, coastConfig: CoastConfig) {

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()

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
      .mapAsync(5){ case (request, action) => action.run(request).map(chr => chr) }

    Http().bindAndHandle(flow, "localhost")
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
