package hulk.auth

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.HttpCookiePair
import akka.http.scaladsl.model.{HttpHeader, HttpMethod}
import akka.stream.ActorMaterializer
import hulk.http.HulkHttpRequest
import hulk.http.request.HttpRequestBody

/**
  * Created by reweber on 09/03/2016
  */
class AuthorizedHttpRequest[T] (method: HttpMethod, path: String, httpHeader: Seq[HttpHeader], body: HttpRequestBody)
                               (requestParams: Map[String, String], queryParams: Query, fragment: Option[String])
                               (cookies: Seq[HttpCookiePair])
                               (user: T)
                               (implicit private val actorMaterializer: ActorMaterializer) extends
  HulkHttpRequest(method, path, httpHeader, body)(requestParams, queryParams, fragment)(cookies)(actorMaterializer)
