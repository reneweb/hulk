package coast.http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by reweber on 18/12/2015
  */
trait Action {
  def run(request: CoastHttpRequest): Option[Future[CoastHttpResponse]]
  def run(version: String, request: CoastHttpRequest): Option[Future[CoastHttpResponse]]
}

object Action {
  def apply(f: CoastHttpRequest => CoastHttpResponse) = new Action {
    override def run(request: CoastHttpRequest) = Some(Future(f(request)))

    override def run(version: String, request: CoastHttpRequest) = None
  }

  def apply(versionedActions: (String, CoastHttpRequest => CoastHttpResponse)*) = new Action {
    override def run(request: CoastHttpRequest) = None

    override def run(version: String, request: CoastHttpRequest) = {
      versionedActions.find(_._1 == version).map(a => Future(a._2(request)))
    }
  }
}

object AsyncAction {
  def apply(f: CoastHttpRequest => Future[CoastHttpResponse]) = new Action {
    override def run(request: CoastHttpRequest) = Some(f(request))

    override def run(version: String, request: CoastHttpRequest) = None
  }

  def apply(versionedActions: (String, CoastHttpRequest => Future[CoastHttpResponse])*) = new Action {
    override def run(request: CoastHttpRequest) = None

    override def run(version: String, request: CoastHttpRequest) = {
      versionedActions.find(_._1 == version).map(a => a._2(request))
    }
  }
}