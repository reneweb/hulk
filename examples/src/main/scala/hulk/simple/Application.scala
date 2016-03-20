package hulk.simple

import akka.http.scaladsl.model.HttpMethods
import hulk.HulkHttpServer
import hulk.http._
import hulk.http.response.Text
import hulk.routing.{*, RouteDef, Router}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by reweber on 26/12/2015
  */
object Application extends App {

  val router = new SimpleRouter()
  HulkHttpServer(router).run()
}

class SimpleRouter() extends Router {
  val simpleController = new SimpleController()

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.GET, "/test") -> simpleController.testGet,
    (HttpMethods.POST, "/test") -> simpleController.testPost,
    (*, "/test/:{testParam}/other") -> simpleController.testParam,
    (*, "/test/:{testParamWithRegex: .*}") -> simpleController.testParam
  )
}

class SimpleController() {
  def testGet = Action { request =>
    Future.successful(Ok())
  }

  def testPost = Action { request =>
    println(request.requestParams.mkString(";"))
    request.body.asJson().map{ jsonOpt =>
      jsonOpt.map(Ok(_)).getOrElse(BadRequest())
    }
  }

  def testParam = Action { request =>
    Future.successful(Ok[Text](request.requestParams.mkString(", ")))
  }
}