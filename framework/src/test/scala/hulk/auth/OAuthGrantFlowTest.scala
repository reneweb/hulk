package hulk.auth

import java.util.Date

import akka.http.scaladsl.model.HttpHeader
import org.apache.commons.codec.binary.Base64
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Await}
import scalaoauth2.provider._

/**
  * Created by reweber on 10/03/2016
  */
class OAuthGrantFlowTest extends Specification with Mockito {

  case class TestUser()

  val authHttpHeader = mock[HttpHeader]
  authHttpHeader.name returns "Authorization"
  authHttpHeader.value() returns s"Basic ${new String(Base64.encodeBase64("testClient:testSecret".getBytes))}"

  "OAuthGrantFlow#code" should {
    "proxy to passed generateTokenAndStoreInfo function" >> {
      val clientId = "clientId"
      val redirectUri = "redirectUri"
      val code = "someCode"
      val generateTokenAndStoreInfo = (clientId: String, redirectUri: String) => Future.successful(code)

      val codeResultFuture = OAuthGrantFlow.code(clientId, redirectUri, generateTokenAndStoreInfo)
      val codeResult = Await.result(codeResultFuture, 5 seconds)

      codeResult must equalTo(code)
    }
  }

  "OAuthGrantFlow#grant" should {
    "return grant result if authorization and grant type is valid" >> {
      val oAuthGrantFlowData = OAuthGrantFlowData(authHttpHeader, "authorization_code", "authCode", "redirectUri", None)

      val grantResultFuture = OAuthGrantFlow.token(oAuthGrantFlowData, getMockedDataHandler)
      val grantResult = Await.result(grantResultFuture, 5 seconds)

      grantResult.authInfo.clientId must beSome("testClient")
      grantResult.authInfo.user must beAnInstanceOf[TestUser]
      grantResult.accessToken must equalTo("accessToken")
    }
    "return grant result if authorization and grant type and scope is valid" >> {
      val oAuthGrantFlowData = OAuthGrantFlowData(authHttpHeader, "authorization_code", "authCode", "redirectUri", Some("testScope"))

      val grantResultFuture = OAuthGrantFlow.token(oAuthGrantFlowData, getMockedDataHandlerWithScope("testScope"))
      val grantResult = Await.result(grantResultFuture, 5 seconds)

      grantResult.authInfo.clientId must beSome("testClient")
      grantResult.authInfo.user must beAnInstanceOf[TestUser]
      grantResult.accessToken must equalTo("accessToken")
      grantResult.scope must beSome("testScope")
    }
    "return error if authorization is invalid" >> {
      val wrongHeader = mock[HttpHeader]
      wrongHeader.name returns "Authorization"
      wrongHeader.value() returns s"Basic ${new String(Base64.encodeBase64("wrongWrong:testSecret".getBytes))}"

      val oAuthGrantFlowData = OAuthGrantFlowData(wrongHeader, "authorization_code", "authCode", "redirectUri", None)

      val grantResultFuture = OAuthGrantFlow.token(oAuthGrantFlowData, getMockedDataHandler)
      Await.result(grantResultFuture, 5 seconds) must throwAn[InvalidClient]
    }
  }

  private def getMockedDataHandlerWithScope(scope: String) = {
    val dataHandler = mock[DataHandler[TestUser]]
    dataHandler.findAuthInfoByCode("authCode") returns Future(Some(AuthInfo(TestUser(), Some("testClient"), Some(scope), None)))
    dataHandler.getStoredAccessToken(any[AuthInfo[TestUser]]) returns Future(Some(AccessToken("accessToken", None, Some(scope), None, new Date())))

    dataHandler
  }

  private def getMockedDataHandler = {
    val dataHandler = mock[DataHandler[TestUser]]
    dataHandler.findAuthInfoByCode("authCode") returns Future(Some(AuthInfo(TestUser(), Some("testClient"), None, None)))
    dataHandler.getStoredAccessToken(any[AuthInfo[TestUser]]) returns Future(Some(AccessToken("accessToken", None, None, None, new Date())))

    dataHandler
  }
}
