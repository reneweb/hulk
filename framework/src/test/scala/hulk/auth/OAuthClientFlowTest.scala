package hulk.auth

import java.util.Date

import akka.http.scaladsl.model.HttpHeader
import org.apache.commons.codec.binary.Base64
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scalaoauth2.provider._

/**
  * Created by reweber on 10/03/2016
  */
class OAuthClientFlowTest extends Specification with Mockito {

  case class TestUser()

  val authHttpHeader = mock[HttpHeader]
  authHttpHeader.name returns "Authorization"
  authHttpHeader.value() returns s"Basic ${new String(Base64.encodeBase64("testClient:testSecret".getBytes))}"

  "OAuthClientFlow#apply" should {
    "return grant result if authorization and grant type is valid" >> {
      val oAuthClientFlowData = OAuthClientFlowData(authHttpHeader, "client_credentials", None)

      val grantResultFuture = OAuthClientFlow(oAuthClientFlowData, getMockedDataHandler).run
      val grantResult = Await.result(grantResultFuture, 5 seconds)

      grantResult.authInfo.clientId must beSome("testClient")
      grantResult.authInfo.user must beAnInstanceOf[TestUser]
      grantResult.accessToken must equalTo("accessToken")
    }
    "return grant result if authorization and grant type and scope is valid" >> {
      val oAuthClientFlowData = OAuthClientFlowData(authHttpHeader, "client_credentials", Some("testScope"))

      val grantResultFuture = OAuthClientFlow(oAuthClientFlowData, getMockedDataHandlerWithScope("testScope")).run
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

      val oAuthClientFlowData = OAuthClientFlowData(wrongHeader, "client_credentials", None)

      val grantResultFuture = OAuthClientFlow(oAuthClientFlowData, getMockedDataHandler).run
      Await.result(grantResultFuture, 5 seconds) must throwAn[InvalidGrant]
    }
    "return error if grant type is invalid" >> {
      val oAuthClientFlowData = OAuthClientFlowData(authHttpHeader, "wrong_grant", None)

      val grantResultFuture = OAuthClientFlow(oAuthClientFlowData, getMockedDataHandler).run
      Await.result(grantResultFuture, 5 seconds) must throwAn[InvalidGrant]
    }
  }

  private def getMockedDataHandlerWithScope(scope: String) = {
    val dataHandler = mock[DataHandler[TestUser]]
    dataHandler.findUser(any[ClientCredentialsRequest]) answers {
      request => request match {
        case r: ClientCredentialsRequest if r.request.scope.isDefined && r.request.scope.get == scope && r.request.grantType == "client_credentials" &&
          r.clientCredential.isDefined && r.clientCredential.get.clientId == "testClient" =>
          Future(Some(TestUser()))
        case _ => Future(None)
      }
    }
    dataHandler.getStoredAccessToken(any[AuthInfo[TestUser]]) returns Future(Some(AccessToken("accessToken", None, Some(scope), None, new Date())))

    dataHandler
  }

  private def getMockedDataHandler = {
    val dataHandler = mock[DataHandler[TestUser]]
    dataHandler.findUser(any[ClientCredentialsRequest]) answers {
      request => request match {
        case r: ClientCredentialsRequest if r.request.grantType == "client_credentials" && r.clientCredential.isDefined && r.clientCredential.get.clientId == "testClient" =>
          Future(Some(TestUser()))
        case _ => Future(None)
      }
    }
    dataHandler.getStoredAccessToken(any[AuthInfo[TestUser]]) returns Future(Some(AccessToken("accessToken", None, None, None, new Date())))

    dataHandler
  }

}