package hulk.filtering

import hulk.http.{HulkHttpRequest, HulkHttpResponse}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by reweber on 13/01/2016
  */
case class RateLimiter(rateLimitBy: RateLimitBy, nrRequest: Int, withinTimeRange: Duration) {
  def apply(f: HulkHttpRequest => HulkHttpResponse): HulkHttpRequest => HulkHttpResponse = {
    f
  }

  def andThen = apply _
}

case class AsyncRateLimiter(rateLimitBy: RateLimitBy, nrRequest: Int, withinTimeRange: Duration) {
  def apply(f: HulkHttpRequest => Future[HulkHttpResponse]): HulkHttpRequest => Future[HulkHttpResponse] = {
    f
  }

  def andThen = apply _
}

trait RateLimitBy
case class IP() extends RateLimitBy
case class Cookie(key: String) extends RateLimitBy

object RateLimitBy {
  def ip = IP()
  def cookie(key: String) = Cookie(key)
}

