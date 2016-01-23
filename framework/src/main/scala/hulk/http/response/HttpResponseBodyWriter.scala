package hulk.http.response

import java.io.{StringWriter, StringReader}

import akka.http.scaladsl.model.ContentTypes
import akka.util.ByteString
import com.github.mustachejava.DefaultMustacheFactory
import play.api.libs.json.{Json => PJson, JsValue}

import scala.collection.JavaConversions
import scala.xml.Elem

/**
  * Created by reweber on 24/12/2015
  */
trait HttpResponseBodyWriter[A <: ResponseFormat] {
  def apply(): HttpResponseBody
}

object HttpResponseBodyWriter {
  implicit def jsonToHttpResponseBodyWriter(json: JsValue): HttpResponseBodyWriter[Json] = {
    new HttpResponseBodyWriter[Json] {
      override def apply(): HttpResponseBody = {
        HttpResponseBody(ContentTypes.`application/json`, ByteString(PJson.stringify(json)))
      }
    }
  }

  implicit def xmlToHttpResponseBodyWriter(xmlElem: Elem): HttpResponseBodyWriter[Xml] = {
    new HttpResponseBodyWriter[Xml] {
      override def apply(): HttpResponseBody = {
        HttpResponseBody(ContentTypes.`text/xml(UTF-8)`, ByteString(xmlElem.mkString))
      }
    }
  }

  implicit def stringToHttpResponseBodyWriter(text: String): HttpResponseBodyWriter[Text] = {
    new HttpResponseBodyWriter[Text] {
      override def apply(): HttpResponseBody = {
        HttpResponseBody(ContentTypes.`text/plain(UTF-8)`, ByteString(text))
      }
    }
  }

  implicit def stringAsHtmlToHttpResponseBodyWriter(text: String): HttpResponseBodyWriter[Html] = {
    new HttpResponseBodyWriter[Html] {
      override def apply(): HttpResponseBody = {
        HttpResponseBody(ContentTypes.`text/html(UTF-8)`, ByteString(text))
      }
    }
  }

  implicit def mustacheAsHtmlToHttpResponseBodyWriter(template: MustacheTemplate[_]): HttpResponseBodyWriter[Html] = {
    new HttpResponseBodyWriter[Html] {
      override def apply(): HttpResponseBody = {
        val writer = new StringWriter()
        val mf = new DefaultMustacheFactory()
        val mustache = mf.compile(new StringReader(template.template), "response")

        mustache.execute(writer, JavaConversions.mapAsJavaMap(template.data))
        writer.close()

        HttpResponseBody(ContentTypes.`text/html(UTF-8)`, ByteString(writer.getBuffer.toString))
      }
    }
  }

  implicit def byteArrayAsBinaryToHttpResponseBodyWriter(bytes: Array[Byte]): HttpResponseBodyWriter[Binary] = {
    new HttpResponseBodyWriter[Binary] {
      override def apply(): HttpResponseBody = {
        HttpResponseBody(ContentTypes.`application/octet-stream`, ByteString(bytes))
      }
    }
  }
}