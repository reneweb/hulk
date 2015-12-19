package coast

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import coast.config.CoastConfig
import coast.http.CoastHttpRequest
import coast.routing.{Filters, Filter, Router}

/**
  * Created by reweber on 18/12/2015
  */
case class CoastHttpServer(router: Router, coastConfig: CoastConfig) {

  implicit val actorSystem = ActorSystem
  implicit val actorMaterializer = ActorMaterializer

  def run() = {
    router match {
      case routerWithFilters: Filters =>
      case _ =>
    }
  }

  private def buildHttpServer() = {

  }
}
