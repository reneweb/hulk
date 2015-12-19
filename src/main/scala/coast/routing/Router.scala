package coast.routing

import coast.http.{Action, RoutingHttpRequest}

/**
  * Created by reweber on 18/12/2015
  */
trait Router {
  def router: (RoutingHttpRequest => Action)
}
