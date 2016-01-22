package hulk.documentation

/**
  * Created by reweber on 21/01/2016
  */
trait ApiDocumentation {
  val title: String
  val description: String
}

trait RouteDocumentation {
  val uri: String
  val method: String
}

trait RequestDocumentation {
  val pathParameters: Map[String, String]
  val queryParameters: Map[String, String]
}

trait ResponseDocumentation {
  val status: Map[Int, String]
}

trait LinkingDocumentation {
  def links: Map[String, String]
}