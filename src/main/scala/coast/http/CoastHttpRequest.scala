package coast.http

import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import coast.http.request.HttpBody

/**
  * Created by reweber on 18/12/2015
  */
case class CoastHttpRequest(method: HttpMethod, uri: Uri, httpHeader: Seq[HttpHeader], body: HttpBody)
                           (implicit akkaHttpRequest: HttpRequest, private val actorMaterializer: ActorMaterializer)

object CoastHttpRequest {
  import coast.http.request.HttpBody._

  implicit private[coast] def fromAkkaHttpRequest(httpRequest: HttpRequest)(implicit actorMaterializer: ActorMaterializer): CoastHttpRequest = {
    CoastHttpRequest(httpRequest.method, httpRequest.uri, httpRequest.headers, httpRequest.entity)(httpRequest, actorMaterializer)
  }
}
