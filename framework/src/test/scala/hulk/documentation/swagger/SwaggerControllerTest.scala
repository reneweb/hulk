package hulk.documentation.swagger

import hulk.http.HulkHttpRequest
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration._

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

      val responseFuture = responseOpt.get
      val response = Await.result(responseFuture, 5 seconds)
      response.statusCode.intValue() must equalTo(200)
      response.body.data.decodeString("UTF-8") must equalTo(json.toString())
    }
  }
}
