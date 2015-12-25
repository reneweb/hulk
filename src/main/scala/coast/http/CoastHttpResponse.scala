package coast.http

import akka.http.scaladsl.model._
import coast.http.response.{HttpResponseBodyWriter, HttpResponseBody}

import scala.collection.immutable.Seq

/**
  * Created by reweber on 18/12/2015
  */
trait CoastHttpResponse {
  val statusCode: StatusCode
  val httpHeader: Seq[HttpHeader]
  val body: HttpResponseBody
}

object CoastHttpResponse {
  implicit private[coast] def toAkkaHttpResponse(coastHttpResponse: CoastHttpResponse): HttpResponse = {
    HttpResponse(coastHttpResponse.statusCode, coastHttpResponse.httpHeader, coastHttpResponse.body)
  }
}

private case class Response(statusCode: StatusCode, body: HttpResponseBody, httpHeader: Seq[HttpHeader]) extends CoastHttpResponse



case object Ok {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(200, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(200, bodyWriter(), httpHeader)
}

case object Created {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(201, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(201, bodyWriter(), httpHeader)
}

case object Accepted {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(202, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(202, bodyWriter(), httpHeader)
}

case object NonAuthoritiveInformation {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(203, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(203, bodyWriter(), httpHeader)
}

case object NoContent {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(204, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(204, bodyWriter(), httpHeader)
}

case object ResetContent {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(205, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(205, bodyWriter(), httpHeader)
}

case object PartialContent {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(206, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(206, bodyWriter(), httpHeader)
}



case object MultipleChoices {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(300, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(300, bodyWriter(), httpHeader)
}

case object MovedPermanently {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(301, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(301, bodyWriter(), httpHeader)
}

case object Found {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(302, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(302, bodyWriter(), httpHeader)
}

case object SeeOther {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(303, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(303, bodyWriter(), httpHeader)
}

case object NotModified {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(304, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(304, bodyWriter(), httpHeader)
}

case object UseProxy {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(305, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(305, bodyWriter(), httpHeader)
}

case object TemporaryRedirect {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(307, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(307, bodyWriter(), httpHeader)
}



case object BadRequest {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(400, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(400, bodyWriter(), httpHeader)
}

case object Unauthorized {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(401, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(401, bodyWriter(), httpHeader)
}

case object PaymentRequired {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(402, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(402, bodyWriter(), httpHeader)
}

case object Forbidden {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(403, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(403, bodyWriter(), httpHeader)
}

case object NotFound {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(404, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(404, bodyWriter(), httpHeader)
}

case object MethodNotAllowed {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(405, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(405, bodyWriter(), httpHeader)
}

case object NotAcceptable {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(406, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(406, bodyWriter(), httpHeader)
}

case object ProxyAuthenticationRequired {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(407, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(407, bodyWriter(), httpHeader)
}

case object RequestTimeout {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(408, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(408, bodyWriter(), httpHeader)
}

case object Conflict {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(409, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(409, bodyWriter(), httpHeader)
}

case object Gone {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(410, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(410, bodyWriter(), httpHeader)
}

case object LengthRequired {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(411, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(411, bodyWriter(), httpHeader)
}

case object PreConditionFailed {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(412, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(412, bodyWriter(), httpHeader)
}

case object HttpResponseBodyTooLarge {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(413, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(413, bodyWriter(), httpHeader)
}

case object RequestUriTooLong {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(414, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(414, bodyWriter(), httpHeader)
}

case object UnsupportedMediaType {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(415, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(415, bodyWriter(), httpHeader)
}

case object RequestRangeNotSatisfiable {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(416, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(416, bodyWriter(), httpHeader)
}

case object ExpectationFailed {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(417, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(417, bodyWriter(), httpHeader)
}



case object InternalServerError {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(500, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(500, bodyWriter(), httpHeader)
}

case object NotImplemented {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(501, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(501, bodyWriter(), httpHeader)
}

case object BadGateway {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(502, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(502, bodyWriter(), httpHeader)
}

case object ServiceUnavailable {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(503, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(503, bodyWriter(), httpHeader)
}

case object GatewayTimeout {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(504, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(504, bodyWriter(), httpHeader)
}

case object HttpVersionNotSupported {
  def apply(body: HttpResponseBody, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(505, body, httpHeader)
  def apply(bodyWriter: HttpResponseBodyWriter, httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(505, bodyWriter(), httpHeader)
}