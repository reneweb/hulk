package hulk.http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by reweber on 18/12/2015
  */
trait Action {
  def run(request: HulkHttpRequest): Option[Future[HulkHttpResponse]]
  def run(version: String, request: HulkHttpRequest): Option[Future[HulkHttpResponse]]
}

object Action {
  def apply(f: HulkHttpRequest => HulkHttpResponse) = new Action {
    override def run(request: HulkHttpRequest) = Some(Future(f(request)))

    override def run(version: String, request: HulkHttpRequest) = None
  }

  def apply(versionedActions: (String, HulkHttpRequest => HulkHttpResponse)*) = new Action {
    override def run(request: HulkHttpRequest) = None

    override def run(version: String, request: HulkHttpRequest) = {
      versionedActions.find(_._1 == version).map(a => Future(a._2(request)))
    }
  }
}

object AsyncAction {
  def apply(f: HulkHttpRequest => Future[HulkHttpResponse]) = new Action {
    override def run(request: HulkHttpRequest) = Some(f(request))

    override def run(version: String, request: HulkHttpRequest) = None
  }

  def apply(versionedActions: (String, HulkHttpRequest => Future[HulkHttpResponse])*) = new Action {
    override def run(request: HulkHttpRequest) = None

    override def run(version: String, request: HulkHttpRequest) = {
      versionedActions.find(_._1 == version).map(a => a._2(request))
    }
  }
}