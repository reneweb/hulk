package coast.routing

import cats.data.Xor
import coast.http.CoastHttpResponse
import coast.routing.Filter.Next

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 27/12/2015
  */
class FilterResult(val result: Xor[Future[CoastHttpResponse], CoastHttpResponse => Future[CoastHttpResponse]])

object FilterResult {
  implicit def responseToFilterResult(coastHttpResponse: Future[CoastHttpResponse]): FilterResult =
    new FilterResult(Xor.Left(coastHttpResponse))

  implicit def responseFuncToFilterResult(coastHttpResponseFunc: CoastHttpResponse => Future[CoastHttpResponse]): FilterResult =
    new FilterResult(Xor.Right(coastHttpResponseFunc))
}
