package coast.http

import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import coast.http.request.HttpRequestBody

/**
  * Created by reweber on 18/12/2015
  */
case class CoastHttpRequest(method: HttpMethod, uri: Uri, httpHeader: Seq[HttpHeader], body: HttpRequestBody, requestParams: Map[String, String])
                           (implicit akkaHttpRequest: HttpRequest, private val actorMaterializer: ActorMaterializer)
