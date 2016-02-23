package hulk.http

import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import hulk.http.response.{ResponseFormat, HttpResponseBodyWriter, HttpResponseBody}

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 18/12/2015
  */
trait HulkHttpResponse {
  val statusCode: StatusCode
  val httpHeader: Seq[HttpHeader]
  val body: HttpResponseBody
}

object HulkHttpResponse {
  implicit private[hulk] def toAkkaHttpResponse(httpResponse: HulkHttpResponse): HttpResponse = {
    HttpResponse(httpResponse.statusCode, httpResponse.httpHeader, httpResponse.body)
  }

  implicit private[hulk] def fromAkkaHttpResponse(httpResponse: HttpResponse)(implicit actorMaterializer: ActorMaterializer, timeout: FiniteDuration = 1 seconds): Future[HulkHttpResponse] = {
    val contentType = httpResponse.entity.contentType
    val body = httpResponse.entity.toStrict(timeout).map(_.data)

    val httpResponseBody = body.map(new HttpResponseBody(contentType, _))
    val response = httpResponseBody.map(body =>
      Response(httpResponse.status, body, httpResponse.headers)
    )

    response
  }
}

private case class Response(statusCode: StatusCode, body: HttpResponseBody, httpHeader: Seq[HttpHeader]) extends HulkHttpResponse

protected trait Empty extends ResponseFormat
protected class EmptyHttpResponseWriter extends HttpResponseBodyWriter[Empty] {
  override def apply(): HttpResponseBody = HttpResponseBody(ContentTypes.NoContentType, ByteString.empty)
}

object Ok {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(200, bodyWriter(), httpHeader)
}

object Created {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(201, bodyWriter(), httpHeader)
}

object Accepted {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(202, bodyWriter(), httpHeader)
}

object NonAuthoritiveInformation {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(203, bodyWriter(), httpHeader)
}

object NoContent {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(204, bodyWriter(), httpHeader)
}

object ResetContent {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(205, bodyWriter(), httpHeader)
}

object PartialContent {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(206, bodyWriter(), httpHeader)
}



object MultipleChoices {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(300, bodyWriter(), httpHeader)
}

object MovedPermanently {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(301, bodyWriter(), httpHeader)
}

object Found {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(302, bodyWriter(), httpHeader)
}

object SeeOther {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(303, bodyWriter(), httpHeader)
}

object NotModified {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(304, bodyWriter(), httpHeader)
}

object UseProxy {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(305, bodyWriter(), httpHeader)
}

object TemporaryRedirect {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(307, bodyWriter(), httpHeader)
}



object BadRequest {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(400, bodyWriter(), httpHeader)
}

object Unauthorized {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(401, bodyWriter(), httpHeader)
}

object PaymentRequired {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(402, bodyWriter(), httpHeader)
}

object Forbidden {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(403, bodyWriter(), httpHeader)
}

object NotFound {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(404, bodyWriter(), httpHeader)
}

object MethodNotAllowed {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(405, bodyWriter(), httpHeader)
}

object NotAcceptable {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(406, bodyWriter(), httpHeader)
}

object ProxyAuthenticationRequired {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(407, bodyWriter(), httpHeader)
}

object RequestTimeout {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(408, bodyWriter(), httpHeader)
}

object Conflict {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(409, bodyWriter(), httpHeader)
}

object Gone {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(410, bodyWriter(), httpHeader)
}

object LengthRequired {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(411, bodyWriter(), httpHeader)
}

object PreconditionFailed {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(412, bodyWriter(), httpHeader)
}

object HttpResponseBodyTooLarge {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(413, bodyWriter(), httpHeader)
}

object RequestUriTooLong {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(414, bodyWriter(), httpHeader)
}

object UnsupportedMediaType {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(415, bodyWriter(), httpHeader)
}

object RequestRangeNotSatisfiable {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(416, bodyWriter(), httpHeader)
}

object ExpectationFailed {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(417, bodyWriter(), httpHeader)
}

object PreconditionRequired {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(428, bodyWriter(), httpHeader)
}

object TooManyRequests {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(429, bodyWriter(), httpHeader)
}

object RequestHeaderFieldsTooLarge {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(431, bodyWriter(), httpHeader)
}



object InternalServerError {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(500, bodyWriter(), httpHeader)
}

object NotImplemented {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(501, bodyWriter(), httpHeader)
}

object BadGateway {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(502, bodyWriter(), httpHeader)
}

object ServiceUnavailable {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(503, bodyWriter(), httpHeader)
}

object GatewayTimeout {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(504, bodyWriter(), httpHeader)
}

object HttpVersionNotSupported {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(505, bodyWriter(), httpHeader)
}

object NetworkAuthenticationRequired {
  def apply[A <: ResponseFormat](bodyWriter: HttpResponseBodyWriter[A] = new EmptyHttpResponseWriter(), httpHeader: Seq[HttpHeader] = Seq()): HulkHttpResponse = Response(511, bodyWriter(), httpHeader)
}