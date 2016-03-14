package hulk.oauth

import java.util.Date

import akka.http.scaladsl.model.HttpMethods
import hulk.HulkHttpServer
import hulk.auth.{Authorized, OAuthPasswordFlow, OAuthPasswordFlowData}
import hulk.http._
import hulk.routing.{RouteDef, Router}
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scalaoauth2.provider._

/**
  * See https://tools.ietf.org/html/rfc6749 for more Info
  */
object OAuthPasswordFlowApplication extends App {
  val router = new OAuthPasswordRouter()
  HulkHttpServer(router).run()
}

class OAuthPasswordRouter() extends Router {
  val oAuthPasswordController = new OAuthPasswordController()

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.POST, "/token") -> oAuthPasswordController.token,
    (HttpMethods.GET, "/restrictedResource") -> oAuthPasswordController.restrictedResource
  )
}

class OAuthPasswordController() {
  def token = AsyncAction { request =>
    val passwordAuthHandler = new OAuthPasswordAuthorizationHandler()
    val passwordFlowData = OAuthPasswordFlowData(request.httpHeader.find(_.name() == "Authorization").get, "client_credentials", "username", "password", None)

    val grantResultFuture = OAuthPasswordFlow(passwordFlowData, passwordAuthHandler).run
    grantResultFuture.map(grantResult => {
      Ok(Json.obj("access_token" -> grantResult.accessToken,
        "refresh_token" -> grantResult.refreshToken,
        "expires_in" -> grantResult.expiresIn))
    })
  }

  val oAuthPasswordProtectedResourceHandler = new OAuthGrantProtectedResourceHandler()

  def restrictedResource = AsyncAction { Authorized(oAuthPasswordProtectedResourceHandler) { request =>
    Future.successful(Ok())
  }}
}

class OAuthPasswordAuthorizationHandler extends AuthorizationHandler[TestUser] {
  //These functions should properly validate the input and store / retrieve the data from a db
  override def validateClient(request: AuthorizationRequest): Future[Boolean] = Future.successful(true)
  override def createAccessToken(authInfo: AuthInfo[TestUser]): Future[AccessToken] =
    Future.successful(AccessToken("accessToken", None, None, None, new Date()))

  override def refreshAccessToken(authInfo: AuthInfo[TestUser], refreshToken: String): Future[AccessToken] =
    Future.successful(AccessToken("accessToken", None, None, None, new Date()))

  override def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[TestUser]]] =
    Future.successful(Some(AuthInfo(TestUser(), Some("clientId"), None, None)))

  override def getStoredAccessToken(authInfo: AuthInfo[TestUser]): Future[Option[AccessToken]] =
    Future(Some(AccessToken("accessToken", None, None, None, new Date())))

  override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[TestUser]]] =
    Future.successful(Some(AuthInfo(TestUser(), Some("clientId"), None, None)))

  override def findUser(request: AuthorizationRequest): Future[Option[TestUser]] = Future.successful(Some(TestUser()))
  override def deleteAuthCode(code: String): Future[Unit] = Future.successful()
}

class OAuthPasswordProtectedResourceHandler extends ProtectedResourceHandler[TestUser] {
  override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[TestUser]]] =
    Future.successful(Some(AuthInfo(TestUser(), Some("clientId"), None, None)))

  override def findAccessToken(token: String): Future[Option[AccessToken]] =
    Future(Some(AccessToken("accessToken", None, None, None, new Date())))
}
