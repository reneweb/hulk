package hulk.oauth

import java.util.Date

import akka.http.scaladsl.model.HttpMethods
import hulk.HulkHttpServer
import hulk.auth.{Authorized, OAuthGrantFlow, OAuthGrantFlowData}
import hulk.http._
import hulk.routing.{RouteDef, Router}
import play.api.libs.json.Json

import scala.concurrent.Future
import scalaoauth2.provider._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * See https://tools.ietf.org/html/rfc6749 for more Info
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

  val exampleProtectedResourceHandler = new ExampleProtectedResourceHandler()

  def restrictedResource = AsyncAction { Authorized(exampleProtectedResourceHandler) { request =>
    Future.successful(Ok())
  }}
}

class GrantAuthorizationHandler extends AuthorizationHandler[TestUser] {
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

class ExampleProtectedResourceHandler extends ProtectedResourceHandler[TestUser] {
  override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[TestUser]]] =
    Future.successful(Some(AuthInfo(TestUser(), Some("clientId"), None, None)))

  override def findAccessToken(token: String): Future[Option[AccessToken]] =
    Future(Some(AccessToken("accessToken", None, None, None, new Date())))
}

case class TestUser()
