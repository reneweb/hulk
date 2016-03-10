package hulk.auth

import hulk.http.{Forbidden, HulkHttpRequest, HulkHttpResponse}

import scala.concurrent.Future
import scalaoauth2.provider._

/**
  * Created by reweber on 06/03/2016
  */
class Authorized[T](dataHandler: ProtectedResourceHandler[T]) {

  def apply(f: HulkHttpRequest => Future[HulkHttpResponse]): HulkHttpRequest => Future[HulkHttpResponse] = {
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

  def andThen(f: HulkHttpRequest => Future[HulkHttpResponse]) = apply(f)

  private def findAccessTokenFromQueryString(request: HulkHttpRequest) = request.queryParams.get("access_token")

  private def findAccessTokenFromHeader(request: HulkHttpRequest) = request.httpHeader
    .find(_.name() == "Authorization")
    .map(_.value())
    .filter(_.startsWith("Bearer"))
    .map(_.drop(7))
}
