package hulk.assets

import akka.http.scaladsl.model.HttpMethods
import hulk.HulkHttpServer
import hulk.http.{BadRequest, AsyncAction, Ok, Action}
import hulk.routing.{RouteDef, Router}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 24/01/2016
  */
object Application extends App {

  val router = new SimpleRouter()
  HulkHttpServer(router).run()
}

class SimpleRouter() extends Router {
  val assetsController = new AssetsController()

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.GET, "/assets/:{file}") -> assetsController.get(Some("examples/src/main/scala/hulk/assets"))
  )
}