package hulk.http.response

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes
import akka.stream.ActorMaterializer
import akka.util.ByteString
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by reweber on 07/02/2016
  */
class HttpResponseBodyTest extends Specification with Mockito {

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()

  "HttpResponseBody#toResponseEntity" should {
    "convert an HttpResponseBody to ResponseEntity" >> {
      val httpResponseBody = HttpResponseBody(ContentTypes.`text/plain(UTF-8)`, ByteString("SomeString"))
      val responseEntity = HttpResponseBody.toResponseEntity(httpResponseBody)

      responseEntity.contentType must equalTo(ContentTypes.`text/plain(UTF-8)`)
      Await.result(responseEntity.toStrict(5 seconds), 5 seconds).data must equalTo(ByteString("SomeString"))
    }
  }
}
