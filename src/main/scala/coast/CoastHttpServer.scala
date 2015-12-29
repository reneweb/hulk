package coast

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import coast.config.CoastConfig
import coast.http.CoastHttpRequest._
import coast.http.CoastHttpResponse._
import scala.concurrent.ExecutionContext.Implicits.global
import coast.routing.{Filters, Router}

import scala.concurrent.Future

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

    val flow: Flow[HttpRequest, HttpResponse, Any] = Flow[HttpRequest]
      .mapAsync(parallelism) { request =>
        val filterResults = filtersSeq.map(_.filter(request))
        val incomingFilterResponseOpt = filterResults.filter(_.result.isLeft).lastOption.flatMap(_.result.swap.toOption)

        val response = incomingFilterResponseOpt.getOrElse(router.router(request).run(request))

        val outgoingFilterResults = filterResults.filter(_.result.isRight).map(_.result.toOption.get)
        outgoingFilterResults
          .foldLeft(response){ case (resp, filter) => resp.flatMap(filter) }
          .map(toAkkaHttpResponse => toAkkaHttpResponse)
      }

    serverSettingsOpt.map { serverSettings =>
      Http().bindAndHandle(flow, interface, port, serverSettings)
    }.getOrElse {
      Http().bindAndHandle(flow, interface, port)
    }
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