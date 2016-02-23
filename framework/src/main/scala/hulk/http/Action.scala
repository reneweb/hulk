package hulk.http

import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Source

import scala.concurrent.Future

/**
  * Created by reweber on 18/12/2015
  */
trait Action

trait SyncAction {
  def run(request: HulkHttpRequest): Option[HulkHttpResponse]
  def run(version: String, request: HulkHttpRequest): Option[HulkHttpResponse]
}

trait AsyncAction {
  def run(request: HulkHttpRequest): Option[Future[HulkHttpResponse]]
  def run(version: String, request: HulkHttpRequest): Option[Future[HulkHttpResponse]]
}

trait WebSocketAction {
  def run(): Option[(Source[Message, _], Message => Unit)]
  def run(version: String): Option[(Source[Message, _], Message => Unit)]
}

object Action {
  def apply(f: HulkHttpRequest => HulkHttpResponse) = new Action with SyncAction {
    override def run(request: HulkHttpRequest) = Some(f(request))
    override def run(version: String, request: HulkHttpRequest) = None
  }

  def apply(versionedActions: (String, HulkHttpRequest => HulkHttpResponse)*) = new Action with SyncAction {
    override def run(request: HulkHttpRequest) = None
    override def run(version: String, request: HulkHttpRequest) = {
      versionedActions.find(_._1 == version).map(a => a._2(request))
    }
  }
}

object AsyncAction {
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
  def apply(sender: Source[Message, _], receiver: Message => Unit) = new Action with WebSocketAction {
    override def run() = Some(sender, receiver)
    override def run(version: String) = None
  }

  def apply(versionedActions: (String, (Source[Message, _], Message => Unit))*) = new Action with WebSocketAction {
    override def run() = None
    override def run(version: String) = versionedActions.find(_._1 == version).map(a => a._2)
  }
}