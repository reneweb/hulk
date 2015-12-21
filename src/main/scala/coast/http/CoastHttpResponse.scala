package coast.http

import akka.http.scaladsl.model._

import scala.collection.immutable.Seq

/**
  * Created by reweber on 18/12/2015
  */
case class CoastHttpResponse(statusCode: StatusCode, httpHeader: Seq[HttpHeader], body: RequestEntity)

object CoastHttpResponse {
  implicit private[coast] def toAkkaHttpResponse(coastHttpResponse: CoastHttpResponse): HttpResponse = {
    HttpResponse(coastHttpResponse.statusCode, coastHttpResponse.httpHeader, coastHttpResponse.body)
  }
}
