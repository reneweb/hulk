package hulk.auth

import java.util.Date

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.Uri.Query
import hulk.http.{HulkHttpRequest, Ok}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scalaoauth2.provider.{AccessToken, AuthInfo, DataHandler}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 10/03/2016
  */
class AuthorizedTest extends Specification with Mockito {

  case class TestUser()
  val accessToken = AccessToken("test", None, None, None, new Date())
  val authInfo = AuthInfo(TestUser(), None, None, None)

  "Authorized#apply" should {
    "pass to action and return response of action if access token found in query param and valid" >> {
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.queryParams returns Query(Map("access_token" -> "test"))

      val dataHandler = mock[DataHandler[TestUser]]
      dataHandler.findAccessToken("test") returns Future(Option(accessToken))
      dataHandler.findAuthInfoByAccessToken(accessToken) returns Future(Option(authInfo))

      val authorized = Authorized(dataHandler)

      def f(request: HulkHttpRequest) = Future.successful(Ok())

      val responseFuture = authorized.apply(f).apply(mockedHttpRequest)
      val response = Await.result(responseFuture, 5 seconds)

      response.statusCode.intValue() must equalTo(200)
    }
    "pass to action and return response of action if access token found in authorization header and valid" >> {
      val authHttpHeader = mock[HttpHeader]
      authHttpHeader.name returns "Authorization"
      authHttpHeader.value() returns "Bearer test"

      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.queryParams returns Query(Map.empty[String, String])
      mockedHttpRequest.httpHeader returns Seq(authHttpHeader)
      val dataHandler = mock[DataHandler[TestUser]]
      dataHandler.findAccessToken("test") returns Future(Option(accessToken))
      dataHandler.findAuthInfoByAccessToken(accessToken) returns Future(Option(authInfo))
      val authorized = Authorized(dataHandler)

      def f(request: HulkHttpRequest) = Future.successful(Ok())

      val responseFuture = authorized.apply(f).apply(mockedHttpRequest)
      val response = Await.result(responseFuture, 5 seconds)

      response.statusCode.intValue() must equalTo(200)
    }
    "return forbidden if access token invalid" >> {
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.queryParams returns Query(Map("access_token" -> "test"))

      val dataHandler = mock[DataHandler[TestUser]]
      dataHandler.findAccessToken(any) returns Future(None)
      val authorized = Authorized(dataHandler)

      def f(request: HulkHttpRequest) = Future.successful(Ok())

      val responseFuture = authorized.apply(f).apply(mockedHttpRequest)
      val response = Await.result(responseFuture, 5 seconds)

      response.statusCode.intValue() must equalTo(403)
    }
    "return forbidden if request does not contain access token field" >> {
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.queryParams returns Query(Map.empty[String, String])
      mockedHttpRequest.httpHeader returns Seq.empty

      val dataHandler = mock[DataHandler[TestUser]]
      val authorized = Authorized(dataHandler)

      def f(request: HulkHttpRequest) = Future.successful(Ok())

      val responseFuture = authorized.apply(f).apply(mockedHttpRequest)
      val response = Await.result(responseFuture, 5 seconds)

      response.statusCode.intValue() must equalTo(403)
    }
  }

  "Authorized#andThen" should {
    "route to apply" >> {
      val mockedHttpRequest = mock[HulkHttpRequest]
      mockedHttpRequest.queryParams returns Query(Map("access_token" -> "test"))

      val dataHandler = mock[DataHandler[TestUser]]
      dataHandler.findAccessToken("test") returns Future(Option(accessToken))
      dataHandler.findAuthInfoByAccessToken(accessToken) returns Future(Option(authInfo))

      val authorized = Authorized(dataHandler)

      def f(request: HulkHttpRequest) = Future.successful(Ok())

      val responseFuture = authorized.andThen(f).apply(mockedHttpRequest)
      val response = Await.result(responseFuture, 5 seconds)

      response.statusCode.intValue() must equalTo(200)
    }
  }
}