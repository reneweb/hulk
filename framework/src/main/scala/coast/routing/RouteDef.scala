package coast.routing

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{HttpMethod, HttpRequest}
import coast.http.{Action, CoastHttpRequest}

/**
  * Created by reweber on 18/12/2015
  */
case class RouteDef(method: Option[HttpMethod], path: Option[Path])

object RouteDef {

  implicit private[coast] def fromAkkaHttpRequest(httpRequest: HttpRequest): RouteDef = {
    new RouteDef(Some(httpRequest.method), Some(httpRequest.uri.path))
  }

  implicit private[coast] def fromCoastHttpRequest(httpRequest: CoastHttpRequest): RouteDef = {
    new RouteDef(Some(httpRequest.method), Some(httpRequest.uri.path))
  }

  implicit private[coast] def fromTupleRoute(t: ((HttpMethod, Path), Action)): (RouteDef, Action) = {
    val (methodWithPath, action) = t
    val (method, uri) = methodWithPath

    (RouteDef(Some(method), Some(uri)), action)
  }

  implicit private[coast] def fromExtendHttpMethodTupleRoute(t: ((ExtendedHttpMethods, Path), Action)): (RouteDef, Action) = {
    val (methodWithPath, action) = t
    val (method, uri) = methodWithPath

    (RouteDef(None, Some(uri)), action)
  }
}

class ExtendedHttpMethods
object * extends ExtendedHttpMethods