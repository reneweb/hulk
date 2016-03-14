package hulk.oauth

import java.util.Date

import akka.http.scaladsl.model.HttpMethods
import hulk.HulkHttpServer
import hulk.auth.{OAuthRefreshTokenFlow, OAuthRefreshTokenFlowData, OAuthClientFlow, OAuthClientFlowData}
import hulk.http.{BadRequest, Ok, AsyncAction, Action}
import hulk.routing.{RouteDef, Router}
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scalaoauth2.provider.{AccessToken, AuthInfo, AuthorizationRequest, AuthorizationHandler}

/**
  * See https://tools.ietf.org/html/rfc6749 for more Info
  */
object OAuthRefreshTokenFlowApplication extends App {
  val router = new OAuthRouter()
  HulkHttpServer(router).run()
}

class OAuthRouter() extends Router {
  val oAuthRefreshTokenController = new OAuthRefreshTokenController()

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.POST, "/token") -> oAuthRefreshTokenController.token,
    (HttpMethods.GET, "/restrictedResource") -> oAuthRefreshTokenController.restrictedResource
  )
}

class OAuthRefreshTokenController() {
  def token = AsyncAction { request =>
    val refreshTokenAuthHandler = new RefreshTokenAuthorizationHandler()
    val refreshTokenFlowData = OAuthRefreshTokenFlowData(request.httpHeader.find(_.name() == "Authorization").get, "refresh_token", "refreshToken")

    val grantResultFuture = OAuthRefreshTokenFlow(refreshTokenFlowData, refreshTokenAuthHandler).run
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

class RefreshTokenAuthorizationHandler extends AuthorizationHandler[TestUser] {
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

case class TestUser()
