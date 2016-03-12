package hulk.auth

import java.util.Date

import akka.http.scaladsl.model.HttpHeader
import org.apache.commons.codec.binary.Base64
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scalaoauth2.provider._

/**
  * Created by reweber on 10/03/2016
  */
class OAuthRefreshTokenFlowTest extends Specification with Mockito {

  case class TestUser()

  val authHttpHeader = mock[HttpHeader]
  authHttpHeader.name returns "Authorization"
  authHttpHeader.value() returns s"Basic ${new String(Base64.encodeBase64("testClient:testSecret".getBytes))}"

  "OAuthRefreshTokenFlow#apply" should {
    "return grant result if authorization and grant type is valid" >> {
      val oAuthRefreshTokenFlowData = OAuthRefreshTokenFlowData(authHttpHeader, "refresh_token", "refreshToken")

      val grantResultFuture = OAuthRefreshTokenFlow(oAuthRefreshTokenFlowData, getMockedDataHandler).run
      val grantResult = Await.result(grantResultFuture, 5 seconds)

      grantResult.authInfo.clientId must beSome("testClient")
      grantResult.authInfo.user must beAnInstanceOf[TestUser]
      grantResult.accessToken must equalTo("accessToken")
      grantResult.refreshToken must beSome("refreshToken")
    }
    "return error if authorization is invalid" >> {
      val wrongHeader = mock[HttpHeader]
      wrongHeader.name returns "Authorization"
      wrongHeader.value() returns s"Basic ${new String(Base64.encodeBase64("wrongWrong:testSecret".getBytes))}"

      val oAuthRefreshTokenFlowData = OAuthRefreshTokenFlowData(wrongHeader, "refresh_token", "refreshToken")

      val grantResultFuture = OAuthRefreshTokenFlow(oAuthRefreshTokenFlowData, getMockedDataHandler).run
      Await.result(grantResultFuture, 5 seconds) must throwAn[InvalidClient]
    }
  }

  private def getMockedDataHandler = {
    val dataHandler = mock[DataHandler[TestUser]]
    dataHandler.findUser(any[RefreshTokenRequest]) answers {
      request => request match {
        case r: RefreshTokenRequest if r.request.grantType == "client_credentials" && r.clientCredential.isDefined && r.clientCredential.get.clientId == "testClient" =>
          Future(Some(TestUser()))
        case _ => Future(None)
      }
    }
    dataHandler.findAuthInfoByRefreshToken("refreshToken") returns Future(Some(AuthInfo(TestUser(), Some("testClient"), None, None)))
    dataHandler.refreshAccessToken(any[AuthInfo[TestUser]], ===("refreshToken")) returns Future(AccessToken("accessToken", Some("refreshToken"), None, None, new Date()))
    dataHandler.getStoredAccessToken(any[AuthInfo[TestUser]]) returns Future(Some(AccessToken("accessToken", Some("refreshToken"), None, None, new Date())))

    dataHandler
  }
}

