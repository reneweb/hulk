package hulk.http.response

import java.io.{ByteArrayInputStream, InputStream, StringReader, InputStreamReader}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes
import akka.stream.ActorMaterializer
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.{Json => PJson}

import scala.io.Codec
import scala.reflect.io.File
import scala.xml.{XML, Elem}

/**
  * Created by reweber on 07/02/2016
  */
class HttpResponseBodyWriterTest extends Specification  with Mockito {

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()

  "HttpResponseBodyWriter#jsonToHttpResponseBodyWriter" should {
    "write json to http response" >> {
      val jsonString = """{"test":"value"}"""
      val json = PJson.parse(jsonString)
      val body = HttpResponseBodyWriter.jsonToHttpResponseBodyWriter(json)()
      val resultString = body.data.decodeString("UTF-8")

      body.contentType must equalTo(ContentTypes.`application/json`)
      resultString must equalTo(jsonString)
    }
  }

  "HttpResponseBodyWriter#xmlToHttpResponseBodyWriter" should {
    "write xml to http response" >> {
      val xmlString = """<test>value</test>"""
      val xml = XML.loadString(xmlString)
      val body = HttpResponseBodyWriter.xmlToHttpResponseBodyWriter(xml)()
      val resultString = body.data.decodeString("UTF-8")

      body.contentType must equalTo(ContentTypes.`text/xml(UTF-8)`)
      resultString must equalTo(xmlString)
    }
  }

  "HttpResponseBodyWriter#stringToHttpResponseBodyWriter" should {
    "write string to http response" >> {
      val string = "someString"
      val body = HttpResponseBodyWriter.stringToHttpResponseBodyWriter(string)()
      val resultString = body.data.decodeString("UTF-8")

      body.contentType must equalTo(ContentTypes.`text/plain(UTF-8)`)
      resultString must equalTo(string)
    }
  }

  "HttpResponseBodyWriter#stringAsHtmlToHttpResponseBodyWriter" should {
    "write string as html to http response" >> {
      val string = "someString"
      val body = HttpResponseBodyWriter.stringAsHtmlToHttpResponseBodyWriter(string)()
      val resultString = body.data.decodeString("UTF-8")

      body.contentType must equalTo(ContentTypes.`text/html(UTF-8)`)
      resultString must equalTo(string)
    }
  }

  "HttpResponseBodyWriter#mustacheAsHtmlToHttpResponseBodyWriter" should {
    "write mustache string template as html to response" >> {
      val string = "someString"
      val template = MustacheTemplate("{{test}}", Map("test" -> string))
      val body = HttpResponseBodyWriter.mustacheAsHtmlToHttpResponseBodyWriter(template)()
      val resultString = body.data.decodeString("UTF-8")

      body.contentType must equalTo(ContentTypes.`text/html(UTF-8)`)
      resultString must equalTo(string)
    }

    "write mustache file template as html to response" >> {
      val string = "someString"
      val fileTemplate = mock[File]
      fileTemplate.reader(Codec.UTF8) returns new InputStreamReader(new ByteArrayInputStream("{{test}}".getBytes))

      val template = MustacheTemplate(fileTemplate, Map("test" -> string))
      val body = HttpResponseBodyWriter.mustacheAsHtmlToHttpResponseBodyWriter(template)()
      val resultString = body.data.decodeString("UTF-8")

      body.contentType must equalTo(ContentTypes.`text/html(UTF-8)`)
      resultString must equalTo(string)
    }

    "write mustache string template with list as html to response" >> {
      val string = "someString"
      val template = MustacheTemplate("{{#testM}}{{test}}{{/testM}}", Map("testM" -> Seq(Map("test" -> string), Map("test" -> string))))
      val body = HttpResponseBodyWriter.mustacheAsHtmlToHttpResponseBodyWriter(template)()
      val resultString = body.data.decodeString("UTF-8")

      body.contentType must equalTo(ContentTypes.`text/html(UTF-8)`)
      resultString must equalTo(string + string)
    }
  }

  "HttpResponseBodyWriter#byteArrayAsBinaryToHttpResponseBodyWriter" should {
    "write byte array as binary to http response" >> {
      val string = "SomeString"
      val body = HttpResponseBodyWriter.byteArrayAsBinaryToHttpResponseBodyWriter(string.getBytes)()
      val resultString = body.data.decodeString("UTF-8")

      body.contentType must equalTo(ContentTypes.`application/octet-stream`)
      resultString must equalTo(string)
    }
  }
}
