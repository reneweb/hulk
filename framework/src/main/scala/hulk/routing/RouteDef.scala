package hulk.routing

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{HttpMethod, HttpRequest}
import hulk.http.{Action, HulkHttpRequest}

/**
  * Created by reweber on 18/12/2015
  */
case class RouteDef(method: Option[HttpMethod], path: String)

object RouteDef {

  implicit private[hulk] def fromTupleRoute(t: ((HttpMethod, String), Action)): (RouteDef, Action) = {
    val (methodWithPath, action) = t
    val (method, uri) = methodWithPath

    (RouteDef(Some(method), uri), action)
  }

  implicit private[hulk] def fromExtendHttpMethodTupleRoute(t: ((ExtendedHttpMethods, String), Action)): (RouteDef, Action) = {
    val (methodWithPath, action) = t
    val (method, uri) = methodWithPath

    (RouteDef(None, uri), action)
  }
}

class ExtendedHttpMethods
object * extends ExtendedHttpMethods