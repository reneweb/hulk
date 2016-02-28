package hulk.websocket

import akka.actor.Actor.Receive
import akka.actor._
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.ws.Message
import akka.stream.actor.ActorPublisherMessage.{Request, Cancel}
import akka.stream.{ClosedShape, FlowShape, ActorMaterializer, OverflowStrategy}
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl._
import hulk.HulkHttpServer
import hulk.http.response.Text
import hulk.http._
import hulk.http.ws.DefaultWebSocketSenderActor
import hulk.routing.{*, RouteDef, Router}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.concurrent.Future

/**
  * Created by reweber on 24/02/2016
  */
object Application extends App {

  val router = new SimpleRouter()
  HulkHttpServer(router).run()
}

class SimpleRouter() extends Router {
  val simpleController = new SimpleController()

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.GET, "/wsTest") -> simpleController.testGet
  )
}

class SimpleController() {
  implicit val system = ActorSystem("system")
  val senderActor = system.actorOf(Props(classOf[DefaultWebSocketSenderActor], None))


  def testGet = WebSocketAction(Source.fromPublisher(ActorPublisher(senderActor)), msg => {
    senderActor ! msg
  })
}
