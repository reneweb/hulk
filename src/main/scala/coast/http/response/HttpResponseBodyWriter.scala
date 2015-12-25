package coast.http.response

import akka.http.scaladsl.model.ContentTypes
import akka.util.ByteString

/**
  * Created by reweber on 24/12/2015
  */
trait HttpResponseBodyWriter[A <: ResponseFormat] {
  def apply(): HttpResponseBody
}

object HttpResponseBodyWriter {
  implicit def jsonToHttpResponseBodyWriter(json: io.circe.Json): HttpResponseBodyWriter[Json] = {
    new HttpResponseBodyWriter[Json] {
      override def apply(): HttpResponseBody = {
        HttpResponseBody(ContentTypes.`application/json`, ByteString(json.spaces2))
      }
    }
  }
}