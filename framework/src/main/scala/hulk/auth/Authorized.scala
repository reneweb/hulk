package hulk.auth

import hulk.filtering.Filter
import hulk.filtering.Filter.Next
import hulk.http.{Forbidden, HulkHttpRequest, HulkHttpResponse}

import scala.concurrent.Future
import scalaoauth2.provider._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 06/03/2016
  */
case class Authorized[T](dataHandler: ProtectedResourceHandler[T]) extends Filter {

  def apply(f: Next): HulkHttpRequest => Future[HulkHttpResponse] = {
    case request =>
      val accessTokenOpt = findAccessTokenFromQueryString(request).orElse(findAccessTokenFromHeader(request))

      accessTokenOpt.map { accessToken =>
        val protectedResourceRequest = new ProtectedResourceRequest(Map(), Map("access_token" -> Seq(accessToken)))
        val authResultFuture = ProtectedResource.handleRequest(protectedResourceRequest, dataHandler)
        authResultFuture.flatMap { authResult => authResult.fold(
          err => Future.successful(Forbidden()),
          success => f(new AuthorizedHttpRequest(request.method, request.path, request.httpHeader, request.body)
                                                (request.requestParams, request.queryParams, request.fragment)
                                                (request.cookies)
                                                (success.user)
                                                (request.actorMaterializer))
        )}
      }.getOrElse(Future.successful(Forbidden()))
  }

  private def findAccessTokenFromQueryString(request: HulkHttpRequest) = request.queryParams.get("access_token")

  private def findAccessTokenFromHeader(request: HulkHttpRequest) = request.httpHeader
    .find(_.name() == "Authorization")
    .map(_.value())
    .filter(_.startsWith("Bearer"))
    .map(_.drop(7))
}

