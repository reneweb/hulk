package coast.versioning

import akka.http.scaladsl.model.HttpMethods
import coast.CoastHttpServer
import coast.config.{Versioning, CoastConfig}
import coast.http._
import coast.routing.{RouteDef, Router}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 11/01/2016
  */
object Application extends App {

  val router = new SimpleRouter()
  val config = new CoastConfig {
    override def versioning: Option[Versioning] = Some(Versioning.path)
  }

  CoastHttpServer(router, Some(config)).run()
}

class SimpleRouter() extends Router {
  val versionedController = new VersionedController()

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.GET, "/test") -> versionedController.testGet
  )
}

class VersionedController() {
  def testGet = Action (
    ("v1", { request =>
      Ok()
    }),
    ("v2", { request =>
      Ok()
    })
  )
}
