package hulk.auth

import akka.http.scaladsl.model.HttpHeader

import scalaoauth2.provider._

/**
  * Created by reweber on 06/03/2016
  */
class OAuthPasswordFlow[T](oAuthPasswordFlowData: OAuthPasswordFlowData, dataHandler: AuthorizationHandler[T]) {

  val headerMap = Map("Authorization", Seq(oAuthPasswordFlowData.authorization.value()))
  val paramMap = Map("grant_type" -> Seq(oAuthPasswordFlowData.grantType),
                     "username" -> Seq(oAuthPasswordFlowData.username),
                     "password" -> Seq(oAuthPasswordFlowData.password)) ++
    oAuthPasswordFlowData.scope.map(s => Map("scope" -> Seq(s))).getOrElse(Map.empty)

  val passwordRequest = new PasswordRequest(new AuthorizationRequest(headerMap, paramMap))
  new Password().handleRequest(passwordRequest, dataHandler)
}

case class OAuthPasswordFlowData(authorization: HttpHeader, grantType: String, username: String, password: String, scope: Option[String])
