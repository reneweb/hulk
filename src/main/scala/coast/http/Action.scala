package coast.http

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 18/12/2015
  */
class Action(f: Either[(CoastHttpRequest => CoastHttpResponse), (CoastHttpRequest => Future[CoastHttpResponse])]) {

  def run(coastHttpRequest: CoastHttpRequest): Future[CoastHttpResponse] = {
    f match {
      case Left(nonFutureF) => Future(nonFutureF(coastHttpRequest))
      case Right(futureF) => futureF(coastHttpRequest)
    }
  }
}

object Action {
  def apply(f: CoastHttpRequest => CoastHttpResponse) = new Action(Left(f))
}

object AsyncAction {
  def apply(f: CoastHttpRequest => Future[CoastHttpResponse]) = new Action(Right(f))
}