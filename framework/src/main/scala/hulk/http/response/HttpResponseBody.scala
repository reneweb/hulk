package hulk.http.response

import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.{ContentType, ResponseEntity}
import akka.util.ByteString

/**
  * Created by reweber on 24/12/2015
  */
case class HttpResponseBody(contentType: ContentType, data: ByteString)

object HttpResponseBody {
  implicit private[hulk] def toResponseEntity(httpResponseBody: HttpResponseBody): ResponseEntity = {
    Strict(httpResponseBody.contentType, httpResponseBody.data)
  }
}