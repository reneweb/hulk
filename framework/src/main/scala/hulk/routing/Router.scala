package hulk.routing

import hulk.http.Action

/**
  * Created by reweber on 18/12/2015
  */
trait Router {
  def router: Map[RouteDef, Action]
}
