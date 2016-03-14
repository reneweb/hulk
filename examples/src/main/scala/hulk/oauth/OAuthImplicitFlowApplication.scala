package hulk.oauth

import java.util.Date

import akka.http.scaladsl.model.HttpMethods
import hulk.HulkHttpServer
import hulk.auth.{Authorized, OAuthImplicitFlow, OAuthImplicitFlowData}
import hulk.http._
import hulk.routing.{RouteDef, Router}
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scalaoauth2.provider._

/**
  * See https://tools.ietf.org/html/rfc6749 for more Info
  */
object OAuthImplicitFlowApplication extends App {
  val router = new OAuthRouter()
  HulkHttpServer(router).run()
}

class OAuthRouter() extends Router {
  val oAuthImplicitController = new OAuthImplicitController()

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.POST, "/token") -> oAuthImplicitController.token,
    (HttpMethods.GET, "/restrictedResource") -> oAuthImplicitController.restrictedResource
  )
}

class OAuthImplicitController() {
  def token = AsyncAction { request =>
    val implicitAuthHandler = new ImplicitAuthorizationHandler()
    val implicitFlowData = OAuthImplicitFlowData(request.httpHeader.find(_.name() == "Authorization").get, "implicit", None)

    val grantResultFuture = OAuthImplicitFlow(implicitFlowData, implicitAuthHandler).run
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

class ImplicitAuthorizationHandler extends AuthorizationHandler[TestUser] {
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
