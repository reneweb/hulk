package hulk.routing

import akka.http.scaladsl.model.HttpMethods
import hulk.http._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by reweber on 06/02/2016
  */
class RouterTest extends Specification with Mockito {

  "Router#router" should {
    "route requests to actions" >> {
      val router = new Router {
        override def router: Map[RouteDef, Action] = Map(
          (HttpMethods.GET, "/test") -> Action(request => Future.successful(Ok()))
        )
      }

      val responseFuture = router.router.get(RouteDef(Some(HttpMethods.GET), "/test")).get.asInstanceOf[AsyncAction].run(mock[HulkHttpRequest]).get
      val response = Await.result(responseFuture, 5 seconds)
      response.statusCode.intValue() must equalTo(200)

    }

    "route requests to actions using wildcard" >> {
      val router = new Router {
        override def router: Map[RouteDef, Action] = Map(
          (*, "/test") -> Action(request => Future.successful(Ok()))
        )
      }

      val responseFuture = router.router.get(RouteDef(None, "/test")).get.asInstanceOf[AsyncAction].run(mock[HulkHttpRequest]).get
      val response = Await.result(responseFuture, 5 seconds)
      response.statusCode.intValue() must equalTo(200)
    }
  }
}
