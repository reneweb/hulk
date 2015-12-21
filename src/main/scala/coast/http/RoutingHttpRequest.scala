package coast.http

import akka.http.scaladsl.model.{HttpRequest, Uri, HttpMethod}

/**
  * Created by reweber on 18/12/2015
  */
case class RoutingHttpRequest(method: HttpMethod, uri: Uri)

object RoutingHttpRequest {
  implicit private[coast] def fromAkkaHttpRequest(httpRequest: HttpRequest): RoutingHttpRequest = {
    RoutingHttpRequest(httpRequest.method, httpRequest.uri)
  }

  implicit private[coast] def fromCoastHttpRequest(httpRequest: CoastHttpRequest): RoutingHttpRequest = {
    RoutingHttpRequest(httpRequest.method, httpRequest.uri)
  }
}
