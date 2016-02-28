package hulk.http.ws

import akka.actor.Actor
import akka.http.scaladsl.model.ws.Message
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}

import scala.collection.mutable

/**
  * Created by reweber on 28/02/2016
  */
class DefaultWebSocketSenderActor(queueSize: Option[Int]) extends ActorPublisher[Message] {
  var queue: mutable.Queue[Message] = mutable.Queue()

  override def receive: Actor.Receive = {
    case msg: Message =>
      queueSize match {
        case Some(size) if queue.size < size =>
          queue.enqueue(msg)
          publishIfNeeded()
        case None =>
          queue.enqueue(msg)
          publishIfNeeded()
        case _ => //ignored
      }

    case Request(cnt) =>
      publishIfNeeded()
    case Cancel => context.stop(self)
    case _ =>

  }

  def publishIfNeeded() = {
    while (queue.nonEmpty && isActive && totalDemand > 0) {
      onNext(queue.dequeue())
    }
  }

}