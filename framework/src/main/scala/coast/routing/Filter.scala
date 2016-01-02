package coast.routing

import coast.http.{CoastHttpResponse, CoastHttpRequest}
import coast.routing.Filter.Next

import scala.concurrent.Future

/**
  * Created by reweber on 18/12/2015
  */
trait Filter {
  def filter(next: Next): CoastHttpRequest => FilterResult
}

object Filter {
  type Next = (CoastHttpResponse => Future[CoastHttpResponse])
}