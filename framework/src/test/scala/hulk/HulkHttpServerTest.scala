package hulk

import akka.http.scaladsl.model.ContentTypes
import akka.util.ByteString
import hulk.http.response.HttpResponseBody
import org.specs2.mutable.Specification

import scala.concurrent.Await

/**
  * Created by reweber on 07/02/2016
  */
class HulkHttpServerTest extends Specification {

  "HulkHttpServer#toResponseEntity" should {
    "convert an HttpResponseBody to ResponseEntity" >> {
      val httpResponseBody = HttpResponseBody(ContentTypes.`text/plain(UTF-8)`, ByteString("SomeString"))
      val responseEntity = HttpResponseBody.toResponseEntity(httpResponseBody)

      responseEntity.contentType must equalTo(ContentTypes.`text/plain(UTF-8)`)
    }
  }
}
