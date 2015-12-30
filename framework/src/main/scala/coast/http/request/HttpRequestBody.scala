package coast.http.request

import akka.http.scaladsl.model.RequestEntity
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Source
import akka.util.ByteString
import cats.data.Xor
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

  def asJson(): Future[JsValue] = {
    requestEntity.toStrict(1 second).map(e => Json.parse(e.data.utf8String))
  }

  def asJson[A](implicit d: Reads[A]): Future[JsResult[A]] = {
    requestEntity.toStrict(1 second).map(e => Json.fromJson(Json.parse(e.data.utf8String)))
  }

  def asXml(): Future[Option[Elem]] = {
    requestEntity.toStrict(1 second).map(e => Try(XML.loadString(e.data.utf8String)).toOption)
  }

  def asText(): Future[String] = {
    requestEntity.toStrict(1 second).map(e => e.data.utf8String)
  }
}

object HttpRequestBody {
  implicit private[coast] def fromRequestEntity(requestEntity: RequestEntity)(implicit actorMaterializer: ActorMaterializer): HttpRequestBody = {
    HttpRequestBody(requestEntity)(actorMaterializer)
  }
}
