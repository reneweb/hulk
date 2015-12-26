package coast.simplefilter

import akka.http.scaladsl.model.{HttpMethods, Uri}
import coast.CoastHttpServer
import coast.http._
import coast.routing.{Filters, Filter, Router}
import io.circe.Json
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 26/12/2015
  */
object Application extends App {

  val router = new SimpleRouter()
  CoastHttpServer(router).run()
}

class SimpleRouter() extends Router with Filters {
  val simpleController = new SimpleController()

  override def router: RoutingHttpRequest => Action = {
    case RoutingHttpRequest(HttpMethods.GET, Uri.Path("/test")) => simpleController.testGet
    case RoutingHttpRequest(HttpMethods.POST, Uri.Path("/test")) => simpleController.testPost
  }

  override def filters: Seq[Filter] = Seq(new SimpleFilter)
}

class SimpleFilter extends Filter {
  override def filter: (CoastHttpRequest) => Option[CoastHttpRequest] = {
    case CoastHttpRequest(HttpMethods.POST, Uri.Path("/needsAuth"), _, _) => None
    case req: CoastHttpRequest => Some(req)
  }
}

class SimpleController() {
  def testGet = Action { request =>
    Ok()
  }

  def testPost = AsyncAction { request =>
    request.body.asJson().map(Ok(_))
  }
}