package coast.http

import akka.http.scaladsl.model.{Uri, HttpMethod}

/**
  * Created by reweber on 18/12/2015
  */
case class RoutingHttpRequest(method: HttpMethod, uri: Uri)
