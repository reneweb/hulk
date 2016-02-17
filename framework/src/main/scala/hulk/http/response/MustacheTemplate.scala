package hulk.http.response

import cats.data.Xor

import scala.reflect.io.File

/**
  * Created by reweber on 23/01/2016
  */

private[hulk] case class MustacheTemplate[A](template: Xor[String, File], data: Map[String, A])

object MustacheTemplate {
  def apply[A](template: String, data: Map[String, A]): MustacheTemplate[A] = MustacheTemplate(Xor.Left(template), data)

  def apply[A](template: File, data: Map[String, A]): MustacheTemplate[A] = MustacheTemplate(Xor.Right(template), data)
}
