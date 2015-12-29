package coast.routing

import cats.data.Xor
import coast.http.CoastHttpResponse

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 27/12/2015
  */
class FilterResult(val result: Xor[Future[CoastHttpResponse], CoastHttpResponse => Future[CoastHttpResponse]])

object DontFilter {
  def apply() = new FilterResult(Xor.right(Future(_)))
}

object IncomingFilter {
  def apply(response: Future[CoastHttpResponse]) = new FilterResult(Xor.left(response))
  def apply(response: CoastHttpResponse) = new FilterResult(Xor.left(Future(response)))
}

object OutgoingFilter {
  def apply(responseFunc: CoastHttpResponse => Future[CoastHttpResponse]) = new FilterResult(Xor.right(responseFunc))
}
