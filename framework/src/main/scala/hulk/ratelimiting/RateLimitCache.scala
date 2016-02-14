package hulk.ratelimiting

import net.sf.ehcache.{Element, CacheManager}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 31/01/2016
  */
trait RateLimitCache {
  def putRateLimitValue(key: String, currNrRequest: Int, withinTimeRange: Duration): Future[_]
  def updateRateLimitValue(key: String, currNrRequest: Int, withinTimeRange: Duration): Future[_]
  def getRateLimitValue(key: String): Future[Option[String]]
}

class DefaultEhCache(cacheName: String = "defaultRateLimiter") extends RateLimitCache {
  val cache = CacheManager.getInstance().addCacheIfAbsent(cacheName)

  override def putRateLimitValue(key: String, currNrRequest: Int, withinTimeRange: Duration): Future[_] = {
    val el = new Element(key, currNrRequest)
    el.setTimeToLive(withinTimeRange.toSeconds.toInt)
    Future(cache.put(el))
  }

  override def updateRateLimitValue(key: String, currNrRequest: Int, withinTimeRange: Duration): Future[_] = {
    val el = new Element(key, currNrRequest)
    Future(cache.put(el))
  }

  override def getRateLimitValue(key: String): Future[Option[String]] = {
    val el = Future(Option(cache.get(key)))
    el.map(e => e.map(_.getObjectValue.toString))
  }
}

