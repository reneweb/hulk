package hulk.versioning

import akka.http.scaladsl.model.HttpMethods
import hulk.HulkHttpServer
import hulk.config.versioning.Versioning
import hulk.config.HulkConfig
import hulk.http._
import hulk.routing.{RouteDef, Router}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 11/01/2016
  */
object Application extends App {

  val router = new SimpleRouter()
  val config = new HulkConfig {
    override def versioning: Option[Versioning] = Some(Versioning.path)
  }

  HulkHttpServer(router, Some(config)).run()
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
