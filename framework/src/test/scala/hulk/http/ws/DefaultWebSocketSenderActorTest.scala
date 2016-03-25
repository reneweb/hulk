package hulk.http.ws

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestProbe
import org.specs2.mutable.Specification

import scala.concurrent.duration._

/**
  * Created by reweber on 28/02/2016
  */
class DefaultWebSocketSenderActorTest extends Specification {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  "DefaultWebSocketSenderActor#receive" should {
    "put message into queue and publish if queue not full and able to publish" >> {
      val actor = system.actorOf(Props( classOf[DefaultWebSocketSenderActor], None))
      val source = Source.fromPublisher(ActorPublisher(actor))
      val probe = TestProbe()

      source.to(Sink.actorRef(probe.ref, "completed")).run()
      actor ! TextMessage("test")

      val result = probe.expectMsg(1000.millis, TextMessage("test"))
      result must equalTo(TextMessage("test"))
    }

    "put message not into queue and not publish if queue full" >> {
      val actor = system.actorOf(Props( classOf[DefaultWebSocketSenderActor], Some(0)))
      val source = Source.fromPublisher(ActorPublisher(actor))
      val probe = TestProbe()

      source.to(Sink.actorRef(probe.ref, "completed")).run()
      actor ! TextMessage("test")

      probe.expectNoMsg()
      probe.msgAvailable must equalTo(false)
    }

    "publish if request received and queue not empty" >> {
      val actor = system.actorOf(Props( classOf[DefaultWebSocketSenderActor], None))
      val source = Source.fromPublisher(ActorPublisher(actor))
      val probe = TestProbe()

      actor ! TextMessage("test")
      val result = source.runWith(TestSink.probe[TextMessage])
        .ensureSubscription()
        .expectNoMsg()
        .request(1)
        .expectNext()

      result must equalTo(TextMessage("test"))
    }
  }
}
