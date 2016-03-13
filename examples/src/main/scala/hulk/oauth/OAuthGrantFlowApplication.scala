package hulk.oauth

import java.util.Date

import akka.http.scaladsl.model.HttpMethods
import hulk.HulkHttpServer
import hulk.auth.{OAuthGrantFlow, OAuthGrantFlowData}
import hulk.http.{BadRequest, Ok, AsyncAction, Action}
import hulk.routing.{RouteDef, Router}
import play.api.libs.json.Json

import scala.concurrent.Future
import scalaoauth2.provider.{AccessToken, AuthInfo, AuthorizationRequest, AuthorizationHandler}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 13/03/2016
  */
object OAuthGrantFlowApplication extends App {
  val router = new OAuthRouter()
  HulkHttpServer(router).run()
}

class OAuthRouter() extends Router {
  val oAuthGrantController = new OAuthGrantController()

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.POST, "/authorization") -> oAuthGrantController.authorization,
    (HttpMethods.POST, "/token") -> oAuthGrantController.token,
    (HttpMethods.GET, "/restrictedResource") -> oAuthGrantController.restrictedResource
  )
}

class OAuthGrantController() {
  def authorization = AsyncAction { request =>
    request.body.asJson().flatMap { jsOpt =>
      val json = jsOpt.getOrElse(throw new IllegalArgumentException())
      val f = (clientId: String, redirectUri: String) => Future.successful("code")
      val codeFuture = OAuthGrantFlow.code((json \ "clientId").as[String], (json \ "redirectUri").as[String], f)

      codeFuture.map(c => Ok(Json.obj("code" -> c)))
    }
  }

  def token = AsyncAction { request =>
    val grantAuthHandler = new GrantAuthorizationHandler()
    val grantFlowData = OAuthGrantFlowData(request.httpHeader.find(_.name() == "Authorization").get, "authorization_code", "authCode", "redirectUri", None)

    val grantResultFuture = OAuthGrantFlow.token(grantFlowData, grantAuthHandler)
    grantResultFuture.map(grantResult => {
      Ok(Json.obj("access_token" -> grantResult.accessToken,
        "refresh_token" -> grantResult.refreshToken,
        "expires_in" -> grantResult.expiresIn))
    })
  }

  def restrictedResource = AsyncAction { request =>
    println(request.requestParams.mkString(";"))
    request.body.asJson().map { jsonOpt =>
      jsonOpt.map(Ok(_)).getOrElse(BadRequest())
    }
  }
}

class GrantAuthorizationHandler extends AuthorizationHandler[TestUser] {
  override def validateClient(request: AuthorizationRequest): Future[Boolean] = ???
  override def createAccessToken(authInfo: AuthInfo[TestUser]): Future[AccessToken] = ???
  override def refreshAccessToken(authInfo: AuthInfo[TestUser], refreshToken: String): Future[AccessToken] = ???
  override def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[TestUser]]] = ???
  override def getStoredAccessToken(authInfo: AuthInfo[TestUser]): Future[Option[AccessToken]] = Future(Some(AccessToken("accessToken", None, None, None, new Date())))
  override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[TestUser]]] = ???
  override def findUser(request: AuthorizationRequest): Future[Option[TestUser]] = Future.successful(Some(TestUser()))
  override def deleteAuthCode(code: String): Future[Unit] = ???
}

case class TestUser()
