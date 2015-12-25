package coast.http.response

/**
  * Created by reweber on 24/12/2015
  */
trait HttpResponseBodyWriter[A <: ResponseFormat] {
  def apply(): HttpResponseBody
}

trait ResponseFormat

trait Json extends ResponseFormat
trait Xml extends ResponseFormat
trait Text extends ResponseFormat
trait Binary extends ResponseFormat