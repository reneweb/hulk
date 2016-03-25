package hulk.server

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import hulk.filtering.Filter.Next
import hulk.filtering.Filter
import hulk.http._
import hulk.ratelimiting.{DefaultEhCache, RateLimitBy, RateLimiter, GlobalRateLimiting}
import hulk.routing.{RouteDef, Router}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by reweber on 07/02/2016
  */
class RequestHandlerTest extends Specification with Mockito {

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()
  val action = Action(request => Future.successful(Ok()))

  "RequestHandler#executeRateLimiting" should {

    "run rate limiting based on http request" >> {
      val ipHttpHeader = mock[HttpHeader]
      ipHttpHeader.is("x-real-ip") returns true
      ipHttpHeader.value() returns "myIp"

      val httpRequest = HttpRequest(headers = scala.collection.immutable.Seq(ipHttpHeader))

      val router = new Router() with GlobalRateLimiting {
        override def router: Map[RouteDef, Action] = Map(
          (HttpMethods.GET, "/route") -> action
        )

        override def rateLimiter: RateLimiter = RateLimiter(new DefaultEhCache("globalRateLimiter"))(RateLimitBy.ip, 5, 5 seconds)
      }

      val handler = new RequestHandler(router, Map.empty, Seq.empty, None)

      val res = (1 to 6).map(_ => Await.result(handler.executeRateLimiting(httpRequest), 5 seconds))
      res.size must equalTo(6)
      res.take(5).map(_.toOption must beSome[HttpRequest])
      res.last.swap.toOption must beSome[HttpResponse]
    }
  }

  "RequestHandler#handleRequest" should {

    "handle request an return response" >> {
      val httpRequest = HttpRequest(method = HttpMethods.GET, uri = Uri("/route"))

      val router = new Router() {
        override def router: Map[RouteDef, Action] = Map(
          (HttpMethods.GET, "/route") -> action
        )
      }

      val handler = new RequestHandler(router, new RouteRegexGenerator(router).generateRoutesWithRegex(), Seq.empty, None)
      val responseFuture = handler.handleRequest(httpRequest)

      val response = Await.result(responseFuture, 5 seconds)
      response.status.intValue() must equalTo(200)
    }

    "return 404 if no appropriate route was found" >> {
      val httpRequest = HttpRequest(method = HttpMethods.GET, uri = Uri("/route/nonExistent"))

      val router = new Router() {
        override def router: Map[RouteDef, Action] = Map(
          (HttpMethods.GET, "/route") -> action
        )
      }

      val handler = new RequestHandler(router, new RouteRegexGenerator(router).generateRoutesWithRegex(), Seq.empty, None)
      val responseFuture = handler.handleRequest(httpRequest)

      val response = Await.result(responseFuture, 5 seconds)
      response.status.intValue() must equalTo(404)
    }

    "filter request when matching filter is existing" >> {
      val httpRequest = HttpRequest(method = HttpMethods.GET, uri = Uri("/route/filtered"))

      val router = new Router() {
        override def router: Map[RouteDef, Action] = Map(
          (HttpMethods.GET, "/route") -> action
        )
      }

      val filter = new Filter {
        override def apply(next: Next): (HulkHttpRequest) => Future[HulkHttpResponse] = request => Future(Unauthorized())
      }

      val handler = new RequestHandler(router, new RouteRegexGenerator(router).generateRoutesWithRegex(), Seq(filter), None)
      val responseFuture = handler.handleRequest(httpRequest)

      val response = Await.result(responseFuture, 5 seconds)
      response.status.intValue() must equalTo(401)
    }
  }
}
