package hulk.http.response

import akka.http.scaladsl.model.ContentTypes
import akka.util.ByteString
import play.api.libs.json.{Json, JsValue}

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
        HttpResponseBody(ContentTypes.`application/json`, ByteString(Json.stringify(json)))
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

  implicit def byteArrayAsBinaryToHttpResponseBodyWriter(bytes: Array[Byte]): HttpResponseBodyWriter[Binary] = {
    new HttpResponseBodyWriter[Binary] {
      override def apply(): HttpResponseBody = {
        HttpResponseBody(ContentTypes.`application/octet-stream`, ByteString(bytes))
      }
    }
  }
}