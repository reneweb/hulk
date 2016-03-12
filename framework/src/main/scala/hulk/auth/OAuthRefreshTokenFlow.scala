package hulk.auth

import akka.http.scaladsl.model.HttpHeader
import scala.concurrent.ExecutionContext.Implicits.global

import scalaoauth2.provider._

/**
  * Created by reweber on 06/03/2016
  */
class OAuthRefreshTokenFlow[T](oAuthRefreshTokenFlowData: OAuthRefreshTokenFlowData, dataHandler: AuthorizationHandler[T]) {

  val headerMap = Map("Authorization" -> Seq(oAuthRefreshTokenFlowData.authorization.value()))
  val paramMap = Map("grant_type" -> Seq(oAuthRefreshTokenFlowData.grantType), "refresh_token" -> Seq(oAuthRefreshTokenFlowData.refreshToken))

  val refreshTokenRequest = new RefreshTokenRequest(new AuthorizationRequest(headerMap, paramMap))
  new RefreshToken().handleRequest(refreshTokenRequest, dataHandler)
}

case class OAuthRefreshTokenFlowData(authorization: HttpHeader, grantType: String, refreshToken: String)
