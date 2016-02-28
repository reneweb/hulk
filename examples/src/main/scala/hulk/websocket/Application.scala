package hulk.websocket

import akka.actor._
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl._
import hulk.HulkHttpServer
import hulk.http._
import hulk.http.ws.DefaultWebSocketSenderActor
import hulk.routing.{RouteDef, Router}

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


  def testGet = WebSocketAction(Source.fromPublisher(ActorPublisher(senderActor)), { msg => msg match {
    case TextMessage.Strict(txt) => senderActor ! TextMessage.Strict(s"Response: $txt")
    case _ => //ignore
  }})
}
