package hulk.auth

import akka.http.scaladsl.model.HttpHeader
import hulk.http.response.Json
import hulk.http.{BadRequest, HulkHttpRequest, Ok}
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future
import scalaoauth2.provider._

/**
  * Created by reweber on 06/03/2016
  */
class OAuthRefreshTokenFlow[T](oAuthRefreshTokenFlowData: OAuthRefreshTokenFlowData, dataHandler: AuthorizationHandler[T]) {

  val headerMap = Map("Authorization", Seq(oAuthRefreshTokenFlowData.authorization.value()))
  val paramMap = Map("grant_type" -> Seq(oAuthRefreshTokenFlowData.grantType), "refresh_token" -> Seq(oAuthRefreshTokenFlowData.refreshToken))

  val refreshTokenRequest = new RefreshTokenRequest(new AuthorizationRequest(headerMap, paramMap))
  new RefreshToken().handleRequest(refreshTokenRequest, dataHandler)
}

case class OAuthRefreshTokenFlowData(authorization: HttpHeader, grantType: String, refreshToken: String)
