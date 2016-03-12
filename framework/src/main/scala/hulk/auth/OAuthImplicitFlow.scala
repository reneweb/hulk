package hulk.auth

import akka.http.scaladsl.model.HttpHeader
import scala.concurrent.ExecutionContext.Implicits.global

import scalaoauth2.provider._

/**
  * Created by reweber on 06/03/2016
  */
class OAuthImplicitFlow[T](oAuthImplicitFlowData: OAuthImplicitFlowData, dataHandler: AuthorizationHandler[T]) {

  private val headerMap = Map("Authorization" -> Seq(oAuthImplicitFlowData.authorization.value()))
  private val paramMap = Map("grant_type" -> Seq(oAuthImplicitFlowData.grantType)) ++
    oAuthImplicitFlowData.scope.map(s => Map("scope" -> Seq(s))).getOrElse(Map.empty)

  private val implicitRequest = new ImplicitRequest(new AuthorizationRequest(headerMap, paramMap))

  def run = new Implicit().handleRequest(implicitRequest, dataHandler)
}

case class OAuthImplicitFlowData(authorization: HttpHeader, grantType: String, scope: Option[String])
