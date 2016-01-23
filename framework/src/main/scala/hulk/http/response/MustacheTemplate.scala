package hulk.http.response

/**
  * Created by reweber on 23/01/2016
  */
case class MustacheTemplate[A](template: String, data: Map[String, A])
