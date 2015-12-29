package coast.routing

import coast.http.{CoastHttpResponse, CoastHttpRequest}

import scala.concurrent.Future

/**
  * Created by reweber on 18/12/2015
  */
trait Filter {
  def filter: CoastHttpRequest => FilterResult
}
