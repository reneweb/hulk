package hulk.auth

import akka.http.scaladsl.model.HttpHeader

import scalaoauth2.provider._

/**
  * Created by reweber on 06/03/2016
  */
class OAuthImplicitFlow[T](oAuthImplicitFlowData: OAuthImplicitFlowData, dataHandler: AuthorizationHandler[T]) {

  val headerMap = Map("Authorization", Seq(oAuthImplicitFlowData.authorization.value()))
  val paramMap = Map("grant_type" -> Seq(oAuthImplicitFlowData.grantType)) ++
    oAuthImplicitFlowData.scope.map(s => Map("scope" -> Seq(s))).getOrElse(Map.empty)

  val implicitRequest = new ImplicitRequest(new AuthorizationRequest(headerMap, paramMap))
  new Implicit().handleRequest(implicitRequest, dataHandler)
}

case class OAuthImplicitFlowData(authorization: HttpHeader, grantType: String, scope: Option[String])
