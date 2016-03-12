package hulk.auth

import akka.http.scaladsl.model.HttpHeader
import scala.concurrent.ExecutionContext.Implicits.global

import scalaoauth2.provider._

/**
  * Created by reweber on 06/03/2016
  */
case class OAuthRefreshTokenFlow[T](oAuthRefreshTokenFlowData: OAuthRefreshTokenFlowData, dataHandler: AuthorizationHandler[T]) {

  private val headerMap = Map("Authorization" -> Seq(oAuthRefreshTokenFlowData.authorization.value()))
  private val paramMap = Map("grant_type" -> Seq(oAuthRefreshTokenFlowData.grantType), "refresh_token" -> Seq(oAuthRefreshTokenFlowData.refreshToken))

  private val refreshTokenRequest = new RefreshTokenRequest(new AuthorizationRequest(headerMap, paramMap))
  def run = new RefreshToken().handleRequest(refreshTokenRequest, dataHandler)
}

case class OAuthRefreshTokenFlowData(authorization: HttpHeader, grantType: String, refreshToken: String)
