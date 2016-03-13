package hulk.oauth

import java.util.Date

import akka.http.scaladsl.model.HttpMethods
import hulk.HulkHttpServer
import hulk.auth.{OAuthClientFlow, OAuthClientFlowData}
import hulk.http._
import hulk.routing.{RouteDef, Router}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaoauth2.provider.{AccessToken, AuthInfo, AuthorizationHandler, AuthorizationRequest}

/**
  * Created by reweber on 13/03/2016
  */
object OAuthClientFlowApplication extends App {
  val router = new OAuthRouter()
  HulkHttpServer(router).run()
}

class OAuthRouter() extends Router {
  val simpleController = new SimpleController()

  override def router: Map[RouteDef, Action] = Map(
    (HttpMethods.POST, "/authorization") -> simpleController.authorization,
    (HttpMethods.GET, "/restrictedResource") -> simpleController.restrictedResource
  )
}

class SimpleController() {
  def authorization = AsyncAction { request =>
    val clientAuthHandler = new ClientAuthorizationHandler()
    val clientFlowData = OAuthClientFlowData(request.httpHeader.find(_.name() == "Authorization").get, "client_credentials", None)

    val grantResultFuture = OAuthClientFlow(clientFlowData, clientAuthHandler).run
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

class ClientAuthorizationHandler extends AuthorizationHandler[TestUser] {
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
