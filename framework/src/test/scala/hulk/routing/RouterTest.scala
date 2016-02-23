package hulk.routing

import akka.http.scaladsl.model.HttpMethods
import hulk.http.{SyncAction, Action, Ok, HulkHttpRequest}
import hulk.ratelimiting.{RateLimitBy, DefaultEhCache, RateLimiter}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by reweber on 06/02/2016
  */
class RouterTest extends Specification with Mockito {

  "Router#router" should {
    "route requests to actions" >> {
      val router = new Router {
        override def router: Map[RouteDef, Action] = Map(
          (HttpMethods.GET, "/test") -> Action(request => Ok())
        )
      }

      val response = router.router.get(RouteDef(Some(HttpMethods.GET), "/test")).get.asInstanceOf[SyncAction].run(mock[HulkHttpRequest]).get

      response.statusCode.intValue() must equalTo(200)

    }

    "route requests to actions using wildcard" >> {
      val router = new Router {
        override def router: Map[RouteDef, Action] = Map(
          (*, "/test") -> Action(request => Ok())
        )
      }

      val response = router.router.get(RouteDef(None, "/test")).get.asInstanceOf[SyncAction].run(mock[HulkHttpRequest]).get
      response.statusCode.intValue() must equalTo(200)
    }
  }
}
