package coast.http

import akka.http.scaladsl.model._

/**
  * Created by reweber on 18/12/2015
  */
case class CoastHttpRequest(method: HttpMethod, uri: Uri, httpHeader: Seq[HttpHeader], body: RequestEntity)(implicit akkaHttpRequest: HttpRequest)

object CoastHttpRequest {
  implicit private[coast] def fromAkkaHttpRequest(implicit httpRequest: HttpRequest): CoastHttpRequest = {
    CoastHttpRequest(httpRequest.method, httpRequest.uri, httpRequest.headers, httpRequest.entity)
  }
}
