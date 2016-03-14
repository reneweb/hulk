package hulk.auth

import akka.http.scaladsl.model.HttpHeader
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scalaoauth2.provider._

/**
  * Created by reweber on 06/03/2016
  */
case class OAuthImplicitFlow[T](oAuthImplicitFlowData: OAuthImplicitFlowData, dataHandler: AuthorizationHandler[T]) {

  private val headerMap = Map("Authorization" -> Seq(oAuthImplicitFlowData.authorization.value()))
  private val paramMap = Map("response_type" -> Seq(oAuthImplicitFlowData.responseType)) ++
    oAuthImplicitFlowData.scope.map(s => Map("scope" -> Seq(s))).getOrElse(Map.empty)

  private val implicitRequest = new ImplicitRequest(new AuthorizationRequest(headerMap, paramMap))

  def run = {
    if(implicitRequest.request.requireParam("response_type") != "token") {
      Future.failed(new InvalidGrant())
    } else {
      new Implicit().handleRequest(implicitRequest, dataHandler)
    }
  }
}

case class OAuthImplicitFlowData(authorization: HttpHeader, responseType: String, scope: Option[String])
