package hulk.ratelimiting

/**
  * Created by reweber on 31/01/2016
  */
trait GlobalRateLimiting {
  def rateLimiter: RateLimiter
}
