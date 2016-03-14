package hulk.auth

import akka.http.scaladsl.model.HttpHeader
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scalaoauth2.provider._

/**
  * Created by reweber on 06/03/2016
  */
class OAuthGrantFlow[T] {
  import OAuthGrantFlow._

  def code(clientId: String, redirectUri: Option[String], generateTokenAndStoreInfo: (ClientId, Option[RedirectUri]) => Future[Code]) = {
    generateTokenAndStoreInfo(clientId, redirectUri)
  }

  def token(oAuthGrantFlowData: OAuthGrantFlowData, dataHandler: AuthorizationHandler[T]) = {

    val headerMap = Map("Authorization" -> Seq(oAuthGrantFlowData.authorization.value()))
    val paramMap = Map("grant_type" -> Seq(oAuthGrantFlowData.grantType), "code" -> Seq(oAuthGrantFlowData.authorizationCode)) ++
      oAuthGrantFlowData.redirectUri.map(r => Map("redirect_uri" -> Seq(r))).getOrElse(Map.empty) ++
      oAuthGrantFlowData.scope.map(s => Map("scope" -> Seq(s))).getOrElse(Map.empty)

    val authorizationCodeRequest = new AuthorizationCodeRequest(new AuthorizationRequest(headerMap, paramMap))
    new AuthorizationCode().handleRequest(authorizationCodeRequest, dataHandler)
  }
}

object OAuthGrantFlow {

  type ClientId = String
  type RedirectUri = String
  type Code = String

  def code[T](clientId: String, redirectUri: Option[String], generateTokenAndStoreInfo: (ClientId, Option[RedirectUri]) => Future[Code]) =
    new OAuthGrantFlow().code(clientId, redirectUri, generateTokenAndStoreInfo)

  def token[T](oAuthGrantFlowData: OAuthGrantFlowData, dataHandler: AuthorizationHandler[T]) =
    new OAuthGrantFlow().token(oAuthGrantFlowData, dataHandler)
}

case class OAuthGrantFlowData(authorization: HttpHeader, grantType: String, authorizationCode: String, redirectUri: Option[String], scope: Option[String])