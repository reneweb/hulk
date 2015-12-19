package coast.routing

import coast.http.CoastHttpRequest

/**
  * Created by reweber on 18/12/2015
  */
trait Filter {
  def filter: (CoastHttpRequest => Option[CoastHttpRequest])
}
