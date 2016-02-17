package hulk.http.response

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.reflect.io.File

/**
  * Created by reweber on 17/02/2016
  */
class MustacheTemplateTest extends Specification with Mockito {

  "MustacheTemplate#apply" should {
    "create mustache template instance based on string if string passed" >> {
      val templateString = "{{test}}"
      val template = MustacheTemplate(templateString, Map.empty[String, String])

      template.template.swap.toOption must equalTo(Some(templateString))
    }

    "create mustache template instance based on file if file passed" >> {
      val templateFile = mock[File]
      val template = MustacheTemplate(templateFile, Map.empty[String, String])

      template.template.isRight must beTrue
    }
  }
}
