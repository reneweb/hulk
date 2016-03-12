package hulk.auth

import akka.http.scaladsl.model.HttpHeader
import scala.concurrent.ExecutionContext.Implicits.global

import scalaoauth2.provider._

/**
  * Created by reweber on 06/03/2016
  */
case class OAuthClientFlow[T](oAuthClientFlowData: OAuthClientFlowData, dataHandler: AuthorizationHandler[T]) {

  val headerMap = Map("Authorization" -> Seq(oAuthClientFlowData.authorization.value()))
  val paramMap = Map("grant_type" -> Seq(oAuthClientFlowData.grantType)) ++
    oAuthClientFlowData.scope.map(s => Map("scope" -> Seq(s))).getOrElse(Map.empty)

  val clientCredentialsRequest = new ClientCredentialsRequest(new AuthorizationRequest(headerMap, paramMap))
  new ClientCredentials().handleRequest(clientCredentialsRequest, dataHandler)

}

case class OAuthClientFlowData(authorization: HttpHeader, grantType: String, scope: Option[String])
