package hulk.ratelimiting

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.HttpCookiePair
import hulk.http.{HulkHttpRequest, Ok}
import net.sf.ehcache.CacheManager
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by reweber on 06/02/2016
  */
class RateLimiterTest extends Specification with BeforeEach with Mockito {
  sequential

  val ipHttpHeader = mock[HttpHeader]
  ipHttpHeader.is("x-real-ip") returns true
  ipHttpHeader.value() returns "myIp"

  val cookie = mock[HttpCookiePair]
  cookie.name returns "key"
  cookie.value returns "myCookie"

  override protected def before: Any = CacheManager.getInstance().clearAll()

  "RateLimiter#apply" should {
    "rate limit by IP if limit exceeded and set to use IP" >> {
      val rateLimiterIp = RateLimiter(new DefaultEhCache)(RateLimitBy.ip, 5, 5 seconds)
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.httpHeader returns Seq(ipHttpHeader)

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 6).map(_ => Await.result(rateLimiterIp.apply(f)(mockedHttpRequest), 5 seconds))

      res.size must equalTo(6)
      res.take(5).map(_.statusCode.intValue() must equalTo(200))
      res.last.statusCode.intValue() must equalTo(429)

    }

    "not rate limit by IP if limit is not exceeded and set to use IP" >> {
      val rateLimiterIp = RateLimiter(new DefaultEhCache)(RateLimitBy.ip, 5, 5 seconds)
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.httpHeader returns Seq(ipHttpHeader)

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 5).map(_ => Await.result(rateLimiterIp.apply(f)(mockedHttpRequest), 5 seconds))

      res.size must equalTo(5)
      res.take(5).map(_.statusCode.intValue() must equalTo(200))
    }

    "rate limit by Cookie if limit exceeded and set to use Cookie" >> {
      val rateLimiterCookie = RateLimiter(new DefaultEhCache)(RateLimitBy.cookie("key"), 5, 5 seconds)
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.cookies returns Seq(cookie)

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 6).map(_ => Await.result(rateLimiterCookie.apply(f)(mockedHttpRequest), 5 seconds))

      res.size must equalTo(6)
      res.take(5).map(_.statusCode.intValue() must equalTo(200))
      res.last.statusCode.intValue() must equalTo(429)
    }

    "not rate limit by Cookie if limit is not exceeded and set to use Cookie" >> {
      val rateLimiterCookie = RateLimiter(new DefaultEhCache)(RateLimitBy.cookie("key"), 5, 5 seconds)
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.cookies returns Seq(cookie)

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 5).map(_ => Await.result(rateLimiterCookie.apply(f)(mockedHttpRequest), 5 seconds))

      res.size must equalTo(5)
      res.take(5).map(_.statusCode.intValue() must equalTo(200))
    }
  }

  "RateLimiter#limitExceeded" should {
    "return true checked by IP if limit exceeded and set to use IP" >> {
      val rateLimiterIp = RateLimiter(new DefaultEhCache)(RateLimitBy.ip, 5, 5 seconds)
      val headers = Seq(ipHttpHeader)
      val cookies = Seq.empty[HttpCookiePair]

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 6).map(_ => Await.result(rateLimiterIp.limitExceeded(headers, cookies), 5 seconds))

      res.size must equalTo(6)
      res.take(5).map(_ must equalTo(false))
      res.last must equalTo(true)
    }

    "return false checked by IP if limit exceeded and set to use IP" >> {
      val rateLimiterIp = RateLimiter(new DefaultEhCache)(RateLimitBy.ip, 5, 5 seconds)
      val headers = Seq(ipHttpHeader)
      val cookies = Seq.empty[HttpCookiePair]

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 5).map(_ => Await.result(rateLimiterIp.limitExceeded(headers, cookies), 5 seconds))

      res.size must equalTo(5)
      res.take(5).map(_ must equalTo(false))
    }

    "return true checked by Cookie if limit exceeded and set to use Cookie" >> {
      val rateLimiterCookie = RateLimiter(new DefaultEhCache)(RateLimitBy.cookie("key"), 5, 5 seconds)
      val headers = Seq.empty[HttpHeader]
      val cookies = Seq(cookie)

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 6).map(_ => Await.result(rateLimiterCookie.limitExceeded(headers, cookies), 5 seconds))

      res.size must equalTo(6)
      res.take(5).map(_ must equalTo(false))
      res.last must equalTo(true)
    }

    "return false checked by Cookie if limit exceeded and set to use Cookie" >> {
      val rateLimiterCookie = RateLimiter(new DefaultEhCache)(RateLimitBy.cookie("key"), 5, 5 seconds)
      val headers = Seq.empty[HttpHeader]
      val cookies = Seq(cookie)

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 5).map(_ => Await.result(rateLimiterCookie.limitExceeded(headers, cookies), 5 seconds))

      res.size must equalTo(5)
      res.take(5).map(_ must equalTo(false))
    }
  }

  "AsyncRateLimiter#apply" should {
    "rate limit by IP if limit exceeded and set to use IP" >> {
      val asyncRateLimiterIp = AsyncRateLimiter(new DefaultEhCache)(RateLimitBy.ip, 5, 5 seconds)
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.httpHeader returns Seq(ipHttpHeader)

      def f(request: HulkHttpRequest) = Future(Ok())

      val res = (1 to 6).map(_ => Await.result(asyncRateLimiterIp.apply(f)(mockedHttpRequest), 5 seconds))

      res.size must equalTo(6)
      res.take(5).map(_.statusCode.intValue() must equalTo(200))
      res.last.statusCode.intValue() must equalTo(429)

    }

    "not rate limit by IP if limit is not exceeded and set to use IP" >> {
      val asyncRateLimiterIp = AsyncRateLimiter(new DefaultEhCache)(RateLimitBy.ip, 5, 5 seconds)
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.httpHeader returns Seq(ipHttpHeader)

      def f(request: HulkHttpRequest) = Future(Ok())

      val res = (1 to 5).map(_ => Await.result(asyncRateLimiterIp.apply(f)(mockedHttpRequest), 5 seconds))

      res.size must equalTo(5)
      res.take(5).map(_.statusCode.intValue() must equalTo(200))
    }

    "rate limit by Cookie if limit exceeded and set to use Cookie" >> {
      val asyncRateLimiterCookie = AsyncRateLimiter(new DefaultEhCache)(RateLimitBy.cookie("key"), 5, 5 seconds)
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.cookies returns Seq(cookie)

      def f(request: HulkHttpRequest) = Future(Ok())

      val res = (1 to 6).map(_ => Await.result(asyncRateLimiterCookie.apply(f)(mockedHttpRequest), 5 seconds))

      res.size must equalTo(6)
      res.take(5).map(_.statusCode.intValue() must equalTo(200))
      res.last.statusCode.intValue() must equalTo(429)
    }

    "not rate limit by Cookie if limit is not exceeded and set to use Cookie" >> {
      val asyncRateLimiterCookie = AsyncRateLimiter(new DefaultEhCache)(RateLimitBy.cookie("key"), 5, 5 seconds)
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.cookies returns Seq(cookie)

      def f(request: HulkHttpRequest) = Future(Ok())

      val res = (1 to 5).map(_ => Await.result(asyncRateLimiterCookie.apply(f)(mockedHttpRequest), 5 seconds))

      res.size must equalTo(5)
      res.take(5).map(_.statusCode.intValue() must equalTo(200))
    }
  }

  "AsyncRateLimiter#limitExceeded" should {
    "return true checked by IP if limit exceeded and set to use IP" >> {
      val asyncRateLimiterIp = AsyncRateLimiter(new DefaultEhCache)(RateLimitBy.ip, 5, 5 seconds)
      val headers = Seq(ipHttpHeader)
      val cookies = Seq.empty[HttpCookiePair]

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 6).map(_ => Await.result(asyncRateLimiterIp.limitExceeded(headers, cookies), 5 seconds))

      res.size must equalTo(6)
      res.take(5).map(_ must equalTo(false))
      res.last must equalTo(true)
    }

    "return false checked by IP if limit exceeded and set to use IP" >> {
      val asyncRateLimiterIp = AsyncRateLimiter(new DefaultEhCache)(RateLimitBy.ip, 5, 5 seconds)
      val headers = Seq(ipHttpHeader)
      val cookies = Seq.empty[HttpCookiePair]

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 5).map(_ => Await.result(asyncRateLimiterIp.limitExceeded(headers, cookies), 5 seconds))

      res.size must equalTo(5)
      res.take(5).map(_ must equalTo(false))
    }

    "return true checked by Cookie if limit exceeded and set to use Cookie" >> {
      val asyncRateLimiterCookie = AsyncRateLimiter(new DefaultEhCache)(RateLimitBy.cookie("key"), 5, 5 seconds)
      val headers = Seq.empty[HttpHeader]
      val cookies = Seq(cookie)

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 6).map(_ => Await.result(asyncRateLimiterCookie.limitExceeded(headers, cookies), 5 seconds))

      res.size must equalTo(6)
      res.take(5).map(_ must equalTo(false))
      res.last must equalTo(true)
    }

    "return false checked by Cookie if limit exceeded and set to use Cookie" >> {
      val asyncRateLimiterCookie = AsyncRateLimiter(new DefaultEhCache)(RateLimitBy.cookie("key"), 5, 5 seconds)
      val headers = Seq.empty[HttpHeader]
      val cookies = Seq(cookie)

      def f(request: HulkHttpRequest) = Ok()

      val res = (1 to 5).map(_ => Await.result(asyncRateLimiterCookie.limitExceeded(headers, cookies), 5 seconds))

      res.size must equalTo(5)
      res.take(5).map(_ must equalTo(false))
    }
  }
}
