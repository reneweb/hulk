package coast

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import coast.config.CoastConfig
import coast.http.RoutingHttpRequest._
import coast.http.CoastHttpRequest._
import coast.http.CoastHttpResponse._
import coast.routing.{Filters, Router}

/**
  * Created by reweber on 18/12/2015
  */
case class CoastHttpServer(router: Router, coastConfig: CoastConfig) {

  implicit val actorSystem = ActorSystem
  implicit val actorMaterializer = ActorMaterializer

  def run() = {
    router match {
      case routerWithFilters: Filters =>
      case _ => buildHttpServer(router)
    }
  }

  private def buildHttpServer(router: Router) = {

    val flow: Flow[HttpRequest, HttpResponse, Any] = Flow[HttpRequest]
      .map(request => (request, router.router(request)))
      .mapAsync(5){ case (request, action) => action.run(request).map(chr => chr) }

    Http().bindAndHandle(flow, "localhost")
  }
}
