package coast.simplefilter

import akka.http.scaladsl.model.{HttpMethods, Uri}
import coast.CoastHttpServer
import coast.http._
import coast.routing._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

  override def filters: Seq[Filter] = Seq(new SimpleAuthFilter, new SimpleLogRequestTimeFilter)
}

class SimpleAuthFilter extends Filter {
  def isAuthenticated = {
    //Authentication stuff happening
    false //We just default to false here
  }

  override def filter: (CoastHttpRequest) => FilterResult = {
    case CoastHttpRequest(HttpMethods.POST, Uri.Path("/needsAuth"), _, _) =>
      if(isAuthenticated) DontFilter() else IncomingFilter(Unauthorized())
    case req: CoastHttpRequest => DontFilter()
  }
}

class SimpleLogRequestTimeFilter extends Filter {
  override def filter: (CoastHttpRequest) => FilterResult = {
    case req: CoastHttpRequest =>
      val currTime = System.currentTimeMillis()
      OutgoingFilter { response: CoastHttpResponse =>

        val t = System.currentTimeMillis() - currTime
        println(s"$t ms")

        Future(response)
      }
  }
}

class SimpleController() {
  def testGet = Action { request =>
    Ok()
  }

  def testPost = AsyncAction { request =>
    //request.body.asJson().map{ jsonOpt =>
    //  jsonOpt.map(Ok(_)).getOrElse(BadRequest())
    //}
    request.body.asJson().map(Ok(_))
  }
}