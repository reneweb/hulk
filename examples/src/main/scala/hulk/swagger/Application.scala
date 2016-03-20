package hulk.swagger

import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import hulk.HulkHttpServer
import hulk.documentation._
import hulk.documentation.swagger._
import hulk.http.{Action, AsyncAction, BadRequest, Ok}
import hulk.routing.{RouteDef, Router}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by reweber on 30/01/2016
  */
object Application extends App {

  val router = new SimpleRouter()
  HulkHttpServer(router).run()
}

class SimpleRouter() extends Router {
  val simpleController = new SimpleController()
  val swagger = new Swagger(new MySwaggerBase(), Seq(new MyTestGetEndpointDoc(), new MyTestPostEndpointDoc()))

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.GET, "/test") -> simpleController.testGet,
    (HttpMethods.POST, "/test") -> simpleController.testPost,
    (HttpMethods.GET, "/apidoc") -> swagger.asController.get
  )
}

class MySwaggerBase extends ApiDocumentation with SwaggerBaseDocumentation {
  override val name: String = getClass.getCanonicalName
  override val description: String = "This is a cool API"
  override val apiVersion: String = "1.0"
  override val consumes: Seq[String] = Seq("application/json")
}

class MyTestGetEndpointDoc extends ApiDocumentation with SwaggerRouteDocumentation {
  override val name: String = "My Get Endpoint"
  override val description: String = "My Get Endpoint"
  override val method: HttpMethod = HttpMethods.GET
  override val path: String = "/test"
  override val response: Seq[ResponseDocumentation] = Seq(ResponseDocumentation(200, Some("This is an Ok response")))
  override val params: Seq[ParameterDocumentation] = Seq()
}

class MyTestPostEndpointDoc extends ApiDocumentation with SwaggerRouteDocumentation {
  override val name: String = "My Post Endpoint"
  override val description: String = "My Post Endpoint"
  override val method: HttpMethod = HttpMethods.POST
  override val path: String = "/test"
  override val response: Seq[ResponseDocumentation] = Seq(ResponseDocumentation(201, Some("This is an CREATED response")))
  override val params: Seq[ParameterDocumentation] = Seq()
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
}
