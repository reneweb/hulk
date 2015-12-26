package coast.simple

import akka.http.scaladsl.model.{HttpMethods, Uri}
import coast.CoastHttpServer
import coast.http.{AsyncAction, Ok, Action, RoutingHttpRequest}
import coast.routing.Router
import io.circe.Json
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 26/12/2015
  */
object Application extends App {

  val router = new SimpleRouter()
  CoastHttpServer(router).run()
}

class SimpleRouter() extends Router {
  val simpleController = new SimpleController()

  override def router: (RoutingHttpRequest) => Action = {
    case RoutingHttpRequest(HttpMethods.GET, Uri.Path("/test")) => simpleController.testGet
    case RoutingHttpRequest(HttpMethods.POST, Uri.Path("/test")) => simpleController.testPost
  }
}

class SimpleController() {
  def testGet = Action { request =>
    Ok(Json.empty)
  }

  def testPost = AsyncAction { request =>
    request.body.asJson().map(Ok(_))
  }
}
