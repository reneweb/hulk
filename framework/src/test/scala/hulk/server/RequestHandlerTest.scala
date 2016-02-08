package hulk.server

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, HttpHeader, HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
import hulk.http.{Action, Ok}
import hulk.ratelimiting.{GlobalRateLimiting, RateLimitBy, RateLimiter}
import hulk.routing.{RouteDef, Router}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by reweber on 07/02/2016
  */
class RequestHandlerTest extends Specification with Mockito {

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()
  val action = Action(request => Ok())

  "RequestHandler#executeRateLimiting" should {

    val ipHttpHeader = mock[HttpHeader]
    ipHttpHeader.is("x-real-ip") returns true
    ipHttpHeader.value() returns "myIp"

    "run rate limiting based on http request" >> {
      val httpRequest = HttpRequest(headers = scala.collection.immutable.Seq(ipHttpHeader))

      val router = new Router() with GlobalRateLimiting {
        override def router: Map[RouteDef, Action] = Map(
          (HttpMethods.GET, "/route") -> action
        )

        override def rateLimiter: RateLimiter = RateLimiter(RateLimitBy.ip, 5, 5 seconds)
      }

      val handler = new RequestHandler(router, Map.empty, Seq.empty, None)

      val res = (1 to 6).map(_ => Await.result(handler.executeRateLimiting(httpRequest), 5 seconds))
      res.size must equalTo(6)
      res.take(5).map(_.toOption must beSome[HttpRequest])
      res.last.swap.toOption must beSome[HttpResponse]
    }
  }
}
