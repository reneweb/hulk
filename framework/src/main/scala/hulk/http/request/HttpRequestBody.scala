package hulk.http.request

import java.io.IOException

import akka.http.scaladsl.model.RequestEntity
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Source
import akka.util.ByteString
import cats.data.Xor
import com.fasterxml.jackson.core.JsonParseException
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try
import scala.xml.{Elem, XML}

/**
  * Created by reweber on 22/12/2015
  */
case class HttpRequestBody(private val requestEntity: RequestEntity)(implicit actorMaterializer: ActorMaterializer) {

  def asStream(): Source[ByteString, AnyRef] = requestEntity.getDataBytes()

  def asRaw(): Future[ByteString] = {
    requestEntity.toStrict(1 second).map(_.data)
  }

  def asJson(): Future[Option[JsValue]] = {
    requestEntity.toStrict(1 second).map(e => parseStringToJsonOpt(e.data.utf8String))
  }

  def asJson[A](implicit d: Reads[A]): Future[JsResult[A]] = {
    requestEntity.toStrict(1 second).map(e => parseStringToJsResult(e.data.utf8String).flatMap(j => Json.fromJson(j)))
  }

  def asXml(): Future[Option[Elem]] = {
    requestEntity.toStrict(1 second).map(e => Try(XML.loadString(e.data.utf8String)).toOption)
  }

  def asText(): Future[String] = {
    requestEntity.toStrict(1 second).map(e => e.data.utf8String)
  }

  private def parseStringToJsonOpt(jsonString: String): Option[JsValue] = Try(Json.parse(jsonString)).toOption

  private def parseStringToJsResult(jsonString: String): JsResult[JsValue] = {
    Try(JsSuccess(Json.parse(jsonString))).getOrElse(JsError("Could not parse body to json"))
  }

}

object HttpRequestBody {
  implicit private[hulk] def fromRequestEntity(requestEntity: RequestEntity)(implicit actorMaterializer: ActorMaterializer): HttpRequestBody = {
    HttpRequestBody(requestEntity)(actorMaterializer)
  }
}
