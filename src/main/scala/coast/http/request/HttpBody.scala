package coast.http.request

import akka.http.scaladsl.model.RequestEntity
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Source
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import cats.data.Xor
import io.circe._
import io.circe.parse._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{Elem, XML}

/**
  * Created by reweber on 22/12/2015
  */
case class HttpBody(private val requestEntity: RequestEntity)(implicit actorMaterializer: ActorMaterializer) {

  def asStream(): Source[ByteString, AnyRef] = requestEntity.getDataBytes()

  def asRaw(): Future[ByteString] = {
    requestEntity.getDataBytes().runWith(Sink.fold(ByteString()) { case (c, el) => c.concat(el) }, actorMaterializer)
  }

  def asJson(): Future[Json] = {
    requestEntity.getDataBytes()
      .runWith(Sink.fold(ByteString()) { case (c, el) => c.concat(el) }, actorMaterializer)
      .map(s => Json.string(s.utf8String))
  }

  def asJson[A](implicit d: Decoder[A]): Future[Xor[Error, A]] = {
    requestEntity.getDataBytes()
      .runWith(Sink.fold(ByteString()) { case (c, el) => c.concat(el) }, actorMaterializer)
      .map(s => decode[A](s.utf8String))
  }

  def asXml(): Future[Elem] = {
    requestEntity.getDataBytes()
      .runWith(Sink.fold(ByteString()) { case (c, el) => c.concat(el) }, actorMaterializer)
      .map(s => XML.loadString(s.utf8String))
  }

  def asText(): Future[String] = {
    requestEntity.getDataBytes()
      .runWith(Sink.fold(ByteString()) { case (c, el) => c.concat(el) }, actorMaterializer)
      .map(s => s.utf8String)
  }
}

object HttpBody {
  implicit private[coast] def fromRequestEntity(requestEntity: RequestEntity)(implicit actorMaterializer: ActorMaterializer): HttpBody = {
    HttpBody(requestEntity)(actorMaterializer)
  }
}
