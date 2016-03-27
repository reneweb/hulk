package hulk.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.Message
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import hulk.filtering.Filter

import scala.concurrent.Future

/**
  * Created by reweber on 18/12/2015
  */
trait Action

trait AsyncAction {
  def run(request: HulkHttpRequest): Option[Future[HulkHttpResponse]]
  def run(version: String, request: HulkHttpRequest): Option[Future[HulkHttpResponse]]
}

trait WebSocketAction {
  def run(request: HulkHttpRequest): Option[(Seq[Filter], Source[Message, _], Message => Unit)]
  def run(version: String, request: HulkHttpRequest): Option[(Seq[Filter], Source[Message, _], Message => Unit)]
}

object Action {
  def apply(f: HulkHttpRequest => Future[HulkHttpResponse]) = new Action with AsyncAction {
    override def run(request: HulkHttpRequest) = Some(f(request))
    override def run(version: String, request: HulkHttpRequest) = None
  }

  def apply(versionedActions: (String, HulkHttpRequest => Future[HulkHttpResponse])*) = new Action with AsyncAction {
    override def run(request: HulkHttpRequest) = None
    override def run(version: String, request: HulkHttpRequest) = {
      versionedActions.find(_._1 == version).map(a => a._2(request))
    }
  }
}

object WebSocketAction {
  def apply(f: HulkHttpRequest => (ActorRef, Message => Unit)) = new Action with WebSocketAction {
    override def run(request: HulkHttpRequest) = {
      val (senderActor, receiverF) = f(request)
      Some(Seq.empty, Source.fromPublisher(ActorPublisher(senderActor)), receiverF)
    }
    override def run(version: String, request: HulkHttpRequest) = None
  }

  def apply(versionedActions: (String, (HulkHttpRequest => (ActorRef, Message => Unit)))*) = new Action with WebSocketAction {
    override def run(request: HulkHttpRequest) = None
    override def run(version: String, request: HulkHttpRequest) = {
      versionedActions.find(_._1 == version).map { a =>
        val (senderActor, receiverF) = a._2(request)
        (Seq.empty, Source.fromPublisher(ActorPublisher(senderActor)), receiverF)}
    }
  }

  def apply(filters: Seq[Filter], f: HulkHttpRequest => (ActorRef, Message => Unit)) = new Action with WebSocketAction {
    override def run(request: HulkHttpRequest) = {
      val (senderActor, receiverF) = f(request)
      Some(filters, Source.fromPublisher(ActorPublisher(senderActor)), receiverF)
    }
    override def run(version: String, request: HulkHttpRequest) = None
  }

  def apply(filters: Seq[Filter], versionedActions: (String, (HulkHttpRequest => (ActorRef, Message => Unit)))*) = new Action with WebSocketAction {
    override def run(request: HulkHttpRequest) = None
    override def run(version: String, request: HulkHttpRequest) = {
      versionedActions.find(_._1 == version).map { a =>
        val (senderActor, receiverF) = a._2(request)
        (filters, Source.fromPublisher(ActorPublisher(senderActor)), receiverF)}
    }
  }
}