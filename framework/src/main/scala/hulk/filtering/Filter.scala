package hulk.filtering

import hulk.filtering.Filter.Next
import hulk.http.{HulkHttpRequest, HulkHttpResponse}

import scala.concurrent.Future

/**
  * Created by reweber on 18/12/2015
  */
trait Filter {
  def apply(next: Next): HulkHttpRequest => Future[HulkHttpResponse]
  def and(next: Next): HulkHttpRequest => Future[HulkHttpResponse] = apply(next)
}

object Filter {
  type Next = (HulkHttpRequest => Future[HulkHttpResponse])
}