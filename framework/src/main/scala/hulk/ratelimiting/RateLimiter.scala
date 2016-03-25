package hulk.ratelimiting

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.HttpCookiePair
import hulk.filtering.Filter
import hulk.filtering.Filter.Next
import hulk.http.{HulkHttpRequest, HulkHttpResponse, TooManyRequests}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by reweber on 13/01/2016
  */
class RateLimiter(rateLimitBy: RateLimitBy, nrRequest: Int, withinTimeRange: Duration, rateLimitCache: RateLimitCache) extends Filter {

  def apply(f: Next): HulkHttpRequest => Future[HulkHttpResponse] = {
    case request => limitExceeded(request.httpHeader, request.cookies).flatMap { limitExceeded =>
      if(limitExceeded) Future(TooManyRequests()) else f(request)
    }
  }

  private[hulk] def limitExceeded(header: Seq[HttpHeader], cookies: Seq[HttpCookiePair]): Future[Boolean] = {
    rateLimitBy match {
      case ip: IP =>
        extractIpFromHeader(header).map { ip =>
          limitExceededForKey(ip.value(), nrRequest, withinTimeRange, rateLimitCache)
        }.getOrElse(Future(false))
      case cookie: Cookie =>
        cookies.find(_.name == cookie.key).map { cookie =>
          limitExceededForKey(cookie.value, nrRequest, withinTimeRange, rateLimitCache)
        }.getOrElse(Future(false))
      case _ => throw new IllegalStateException("Invalid rate limit type")
    }
  }

  private def limitExceededForKey(key: String, nrRequest: Int, withinTimeRange: Duration, rateLimitCache: RateLimitCache): Future[Boolean] = {
    rateLimitCache.getRateLimitValue(key).flatMap {
      case Some(el) if el.toInt >= nrRequest => Future(true)
      case _ if nrRequest == 0 => Future(true)
      case Some(el) => rateLimitCache.updateRateLimitValue(key, el.toInt + 1, withinTimeRange).map(_ => false)
      case _ => rateLimitCache.putRateLimitValue(key, 1, withinTimeRange).map(_ => false)
    }
  }

  private def extractIpFromHeader(headers: Seq[HttpHeader]) = {
    headers.find(_.is("x-forwarded-for")).orElse(headers.find(_.is("remote-address"))).orElse(headers.find(_.is("x-real-ip")))
  }
}

object RateLimiter {
  def apply(rateLimitBy: RateLimitBy, nrRequest: Int, withinTimeRange: Duration) =
    new RateLimiter(rateLimitBy, nrRequest, withinTimeRange, new DefaultEhCache())

  def apply(rateLimitCache: RateLimitCache)(rateLimitBy: RateLimitBy, nrRequest: Int, withinTimeRange: Duration) =
    new RateLimiter(rateLimitBy, nrRequest, withinTimeRange, rateLimitCache)
}

trait RateLimitBy
case class IP() extends RateLimitBy
case class Cookie(key: String) extends RateLimitBy

object RateLimitBy {
  def ip = IP()
  def cookie(key: String) = Cookie(key)
}