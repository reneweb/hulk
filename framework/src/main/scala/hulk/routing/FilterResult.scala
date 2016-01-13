package hulk.routing

import cats.data.Xor
import hulk.http.HulkHttpResponse

import scala.concurrent.Future

/**
  * Created by reweber on 27/12/2015
  */
class FilterResult(val result: Xor[Future[HulkHttpResponse], HulkHttpResponse => Future[HulkHttpResponse]])

object FilterResult {
  implicit def responseToFilterResult(httpResponse: Future[HulkHttpResponse]): FilterResult =
    new FilterResult(Xor.Left(httpResponse))

  implicit def responseFuncToFilterResult(httpResponseFunc: HulkHttpResponse => Future[HulkHttpResponse]): FilterResult =
    new FilterResult(Xor.Right(httpResponseFunc))
}
