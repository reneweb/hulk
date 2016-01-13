package hulk.http.response

/**
  * Created by reweber on 25/12/2015
  */
trait ResponseFormat

trait Json extends ResponseFormat
trait Xml extends ResponseFormat
trait Text extends ResponseFormat
trait Binary extends ResponseFormat
trait Html extends ResponseFormat
