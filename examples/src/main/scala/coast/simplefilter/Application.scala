package coast.simplefilter

import akka.http.scaladsl.model.{HttpMethod, HttpMethods, Uri}
import coast.CoastHttpServer
import coast.http._
import coast.routing.Filter._
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

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.GET, "/test") -> simpleController.testGet,
    (HttpMethods.POST, "/test") -> simpleController.testPost
  )

  override def filters: Seq[Filter] = Seq(new SimpleLogRequestTimeFilter, new SimpleAuthFilter)
}

class SimpleAuthFilter extends Filter {
  def isAuthenticated = {
    //Authentication stuff happening
    false //We just default to false here
  }

  override def filter(next: Next): (CoastHttpRequest) => FilterResult = {
    case CoastHttpRequest(HttpMethods.POST, Uri.Path("/needsAuth"), _, _, _) =>
      if(isAuthenticated) next else Future(Unauthorized())
    case req: CoastHttpRequest => next
  }
}

class SimpleLogRequestTimeFilter extends Filter {
  override def filter(next: Next): (CoastHttpRequest) => FilterResult = {
    case req: CoastHttpRequest =>
      val currTime = System.currentTimeMillis()
      next andThen { response =>

        val t = System.currentTimeMillis() - currTime
        println(s"$t ms")

        response
      }
  }
}

class SimpleController() {
  def testGet = Action { request =>
    Ok()
  }

  def testPost = AsyncAction { request =>
    request.body.asJson().map{ jsonOpt =>
      jsonOpt.map(Ok(_)).getOrElse(BadRequest())
    }
  }
}