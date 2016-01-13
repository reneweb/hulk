package hulk.http

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpCookiePair
import akka.stream.ActorMaterializer
import hulk.http.request.HttpRequestBody

/**
  * Created by reweber on 18/12/2015
  */
case class HulkHttpRequest(method: HttpMethod, path: String, httpHeader: Seq[HttpHeader], body: HttpRequestBody)
                          (requestParams: Map[String, String], queryParams: Query, fragment: Option[String])
                          (cookies: Seq[HttpCookiePair])
                          (implicit private val actorMaterializer: ActorMaterializer)
