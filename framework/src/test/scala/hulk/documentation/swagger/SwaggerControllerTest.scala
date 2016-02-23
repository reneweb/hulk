package hulk.documentation.swagger

import akka.http.scaladsl.model.HttpRequest
import hulk.config.HulkConfig
import hulk.config.versioning.{Versioning, AcceptHeaderVersioning}
import hulk.http.HulkHttpRequest
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json

import scala.concurrent.Await
import concurrent.duration._

/**
  * Created by reweber on 03/02/2016
  */
class SwaggerControllerTest extends Specification with Mockito {

  val mockedHttpRequest = mock[HulkHttpRequest]

  "SwaggerController#get" should {
    "return swagger json response" >> {
      val json = Json.obj("someObj" -> 123)
      val controller = new SwaggerController(json)
      val action = controller.get

      val responseOpt = action.run(mockedHttpRequest)

      val response = responseOpt.get
      response.statusCode.intValue() must equalTo(200)
      response.body.data.decodeString("UTF-8") must equalTo(json.toString())
    }
  }
}
