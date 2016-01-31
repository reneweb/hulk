package hulk.ratelimiter

import akka.http.scaladsl.model.HttpMethods
import hulk.HulkHttpServer
import hulk.ratelimiting._
import hulk.http.{Action, Ok, _}
import hulk.routing.{RouteDef, Router}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by reweber on 16/01/2016
  */
object Application extends App {

  val router = new SimpleRouter()

  HulkHttpServer(router).run()
}

class SimpleRouter() extends Router with GlobalRateLimiting {
  val rateLimitedController = new RateLimitedController()
  val rateLimitedWithCustomCacheController = new RateLimitedWithCustomCacheController()

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.GET, "/simple") -> rateLimitedController.simple,
    (HttpMethods.GET, "/multiple") -> rateLimitedController.multiple,
    (HttpMethods.GET, "/testCustom") -> rateLimitedWithCustomCacheController.testCustom
  )

  override def rateLimiter: RateLimiter = RateLimiter(RateLimitBy.ip, 5, 1 second)
}

class RateLimitedController() {
  def simple = AsyncAction { RateLimiter(RateLimitBy.ip, 5, 2 seconds) { request =>
    Ok()
  }}

  def multiple = AsyncAction { AsyncRateLimiter(RateLimitBy.ip, 5, 2 seconds) andThen RateLimiter(RateLimitBy.cookie("session"), 3, 4 seconds) { request =>
    Ok()
  }}
}

class RateLimitedWithCustomCacheController() {
  val myRateLimitCache = new RateLimitCache {
    val cache = collection.mutable.Map.empty[String, Int]

    //Element is not in cache
    override def putRateLimitValue(key: String, currNrRequest: Int, withinTimeRange: Duration): Future[_] = {
      //Make sure that the key gets removed after time range is exceeded
      //...

      Future(cache.put(key, currNrRequest))
    }

    //Search for element in cache
    override def getRateLimitValue(key: String): Future[Option[String]] = {
      Future(cache.get(key).map(_.toString))
    }

    //Element was found in cache, but limit is not exceeded, thus update to new value
    //In this case we do the same thing as with creating the val so just call the put method
    override def updateRateLimitValue(key: String, currNrRequest: Int, withinTimeRange: Duration): Future[_] =
      putRateLimitValue(key, currNrRequest, withinTimeRange)
  }

  val rateLimiterWithCache = RateLimiter(myRateLimitCache) _

  def testCustom = AsyncAction { rateLimiterWithCache(RateLimitBy.ip, 5, 2 seconds) { request =>
    Ok()
  }}
}
