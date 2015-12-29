package coast.http

import akka.http.scaladsl.model._
import akka.util.ByteString
import coast.http.response.{ResponseFormat, HttpResponseBodyWriter, HttpResponseBody}

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
  Ok()
  implicit private[coast] def toAkkaHttpResponse(coastHttpResponse: CoastHttpResponse): HttpResponse = {
    HttpResponse(coastHttpResponse.statusCode, coastHttpResponse.httpHeader, coastHttpResponse.body)
  }
}

private case class Response(statusCode: StatusCode, body: HttpResponseBody, httpHeader: Seq[HttpHeader]) extends CoastHttpResponse

protected trait Empty extends ResponseFormat
protected class EmptyHttpResponseWriter extends HttpResponseBodyWriter[Empty] {
  override def apply(): HttpResponseBody = HttpResponseBody(ContentTypes.NoContentType, ByteString.empty)
}



object Ok {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(200, bodyWriter(), httpHeader)
}

object Created {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(201, bodyWriter(), httpHeader)
}

object Accepted {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(202, bodyWriter(), httpHeader)
}

object NonAuthoritiveInformation {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(203, bodyWriter(), httpHeader)
}

object NoContent {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(204, bodyWriter(), httpHeader)
}

object ResetContent {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(205, bodyWriter(), httpHeader)
}

object PartialContent {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(206, bodyWriter(), httpHeader)
}



object MultipleChoices {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(300, bodyWriter(), httpHeader)
}

object MovedPermanently {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(301, bodyWriter(), httpHeader)
}

object Found {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(302, bodyWriter(), httpHeader)
}

object SeeOther {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(303, bodyWriter(), httpHeader)
}

object NotModified {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(304, bodyWriter(), httpHeader)
}

object UseProxy {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(305, bodyWriter(), httpHeader)
}

object TemporaryRedirect {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(307, bodyWriter(), httpHeader)
}



object BadRequest {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(400, bodyWriter(), httpHeader)
}

object Unauthorized {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(401, bodyWriter(), httpHeader)
}

object PaymentRequired {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(402, bodyWriter(), httpHeader)
}

object Forbidden {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(403, bodyWriter(), httpHeader)
}

object NotFound {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(404, bodyWriter(), httpHeader)
}

object MethodNotAllowed {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(405, bodyWriter(), httpHeader)
}

object NotAcceptable {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(406, bodyWriter(), httpHeader)
}

object ProxyAuthenticationRequired {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(407, bodyWriter(), httpHeader)
}

object RequestTimeout {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(408, bodyWriter(), httpHeader)
}

object Conflict {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(409, bodyWriter(), httpHeader)
}

object Gone {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(410, bodyWriter(), httpHeader)
}

object LengthRequired {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(411, bodyWriter(), httpHeader)
}

object PreConditionFailed {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(412, bodyWriter(), httpHeader)
}

object HttpResponseBodyTooLarge {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(413, bodyWriter(), httpHeader)
}

object RequestUriTooLong {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(414, bodyWriter(), httpHeader)
}

object UnsupportedMediaType {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(415, bodyWriter(), httpHeader)
}

object RequestRangeNotSatisfiable {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(416, bodyWriter(), httpHeader)
}

object ExpectationFailed {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(417, bodyWriter(), httpHeader)
}



object InternalServerError {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(500, bodyWriter(), httpHeader)
}

object NotImplemented {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(501, bodyWriter(), httpHeader)
}

object BadGateway {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(502, bodyWriter(), httpHeader)
}

object ServiceUnavailable {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(503, bodyWriter(), httpHeader)
}

object GatewayTimeout {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(504, bodyWriter(), httpHeader)
}

object HttpVersionNotSupported {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): CoastHttpResponse = Response(505, bodyWriter(), httpHeader)
}