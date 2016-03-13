package hulk.oauth

import java.util.Date

import akka.http.scaladsl.model.HttpMethods
import hulk.HulkHttpServer
import hulk.auth.{OAuthPasswordFlow, OAuthPasswordFlowData}
import hulk.http.{BadRequest, Ok, AsyncAction, Action}
import hulk.routing.{RouteDef, Router}
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scalaoauth2.provider.{AccessToken, AuthInfo, AuthorizationRequest, AuthorizationHandler}

/**
  * See https://tools.ietf.org/html/rfc6749 for more Info
  */
object OAuthPasswordFlowApplication extends App {
  val router = new OAuthRouter()
  HulkHttpServer(router).run()
}

class OAuthRouter() extends Router {
  val oAuthPasswordController = new OAuthPasswordController()

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.POST, "/token") -> oAuthPasswordController.token,
    (HttpMethods.GET, "/restrictedResource") -> oAuthPasswordController.restrictedResource
  )
}

class OAuthPasswordController() {
  def token = AsyncAction { request =>
    val passwordAuthHandler = new PasswordAuthorizationHandler()
    val passwordFlowData = OAuthPasswordFlowData(request.httpHeader.find(_.name() == "Authorization").get, "client_credentials", "username", "password", None)

    val grantResultFuture = OAuthPasswordFlow(passwordFlowData, passwordAuthHandler).run
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

class PasswordAuthorizationHandler extends AuthorizationHandler[TestUser] {
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
