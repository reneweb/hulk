package hulk.http.request

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, RequestEntity}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import akka.stream.javadsl.Source
import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.xml.Elem

/**
  * Created by reweber on 07/02/2016
  */
class HttpRequestBodyTest extends Specification with Mockito {

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()

  "HttpRequestBody#asStream" should {
    "pass body as stream" >> {
      val rawEntity = HttpEntity.Strict(ContentTypes.`application/octet-stream`, ByteString("SomeString"))
      val body = HttpRequestBody(rawEntity)
      val bodyStream = body.asStream()

      Sink.foreach[String](s => s must equalTo("SomeString"))
      bodyStream must haveClass[Source[ByteString, AnyRef]]
    }
  }

  "HttpRequestBody#asRaw" should {
    "pass body as raw bytestring" >> {
      val rawEntity = HttpEntity.Strict(ContentTypes.`application/octet-stream`, ByteString("SomeString"))
      val body = HttpRequestBody(rawEntity)

      val contentFuture = body.asRaw()
      val content = Await.result(contentFuture, 5 seconds)

      content must equalTo(ByteString("SomeString"))
    }
  }

  "HttpRequestBody#asText" should {
    "pass body as text" >> {
      val rawEntity = HttpEntity.Strict(ContentTypes.`text/plain(UTF-8)`, ByteString("SomeString"))
      val body = HttpRequestBody(rawEntity)

      val contentFuture = body.asText()
      val content = Await.result(contentFuture, 5 seconds)

      content must equalTo("SomeString")
    }
  }

  "HttpRequestBody#asXml" should {
    "pass body as xml" >> {
      val xml = """<test>SomeString </test>"""
      val rawEntity = HttpEntity.Strict(ContentTypes.`text/xml(UTF-8)`, ByteString(xml))
      val body = HttpRequestBody(rawEntity)

      val contentFuture = body.asXml()
      val content = Await.result(contentFuture, 5 seconds)

      content must haveClass[Some[Elem]]
      content.get.toString must equalTo(xml)
    }
  }

  "HttpRequestBody#asJson" should {
    "return body as json" >> {
      val json = """{"test":"SomeString"}"""
      val rawEntity = HttpEntity.Strict(ContentTypes.`application/json`, ByteString(json))
      val body = HttpRequestBody(rawEntity)

      val contentFuture = body.asJson()
      val content = Await.result(contentFuture, 5 seconds)

      content must haveClass[Some[JsValue]]
      content.get.toString must equalTo(json)
    }

    "return model from json data" >> {
      case class Test(test:String)
      implicit def testReads = new Reads[Test] {
        override def reads(json: JsValue): JsResult[Test] = JsSuccess(Test((json \ "test").as[String]))
      }

      val json = """{"test":"SomeString"}"""

      val rawEntity = HttpEntity.Strict(ContentTypes.`application/json`, ByteString(json))
      val body = HttpRequestBody(rawEntity)

      val contentFuture = body.asJson[Test]
      val content = Await.result(contentFuture, 5 seconds)

      content must haveClass[JsSuccess[JsValue]]
      content.get.test must equalTo("SomeString")
    }

    "return js failure if json data broken" >> {
      case class Test(test:String)
      implicit def testReads = new Reads[Test] {
        override def reads(json: JsValue): JsResult[Test] = JsSuccess(Test((json \ "test").as[String]))
      }

      val json = """{"test":"broken"""

      val rawEntity = HttpEntity.Strict(ContentTypes.`application/json`, ByteString(json))
      val body = HttpRequestBody(rawEntity)

      val contentFuture = body.asJson[Test]
      val content = Await.result(contentFuture, 5 seconds)

      content must haveClass[JsError]
    }
  }
}
