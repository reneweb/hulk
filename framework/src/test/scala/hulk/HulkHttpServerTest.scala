package hulk

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, ContentTypes}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import hulk.http.{Action, Ok}
import hulk.http.response.HttpResponseBody
import hulk.routing.{RouteDef, Router}
import org.specs2.mutable.Specification

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._

/**
  * Created by reweber on 07/02/2016
  */
class HulkHttpServerTest extends Specification {

  val action = Action(request => Future.successful(Ok()))

  "HulkHttpServer#run" should {
    "create http server" >> {
      val router = new Router() {
        override def router: Map[RouteDef, Action] = Map(
          (HttpMethods.GET, "/route") -> action
        )
      }

      val serverBinding = Await.result(HulkHttpServer(router).run(), 5 seconds)
      serverBinding.localAddress.getHostName must equalTo("localhost")
      serverBinding.localAddress.getPort must equalTo(10000)
    }
  }
}
