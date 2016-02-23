package hulk.server

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.UpgradeToWebsocket
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import cats.data.Xor
import hulk.config.HulkConfig
import hulk.config.versioning.{AcceptHeaderVersioning, AcceptVersionHeaderVersioning, PathVersioning}
import hulk.filtering.Filter
import hulk.http._
import hulk.ratelimiting.GlobalRateLimiting
import hulk.routing.Router

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
  * Created by reweber on 01/02/2016
  */
class RequestHandler(router: Router, routes: Map[RouteDefWithRegex, Action], filters: Seq[Filter], hulkConfig: Option[HulkConfig])
                    (implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) {

  def executeRateLimiting(request: HttpRequest): Future[Xor[HttpResponse, HttpRequest]] = {
    router match {
      case rateLimiting: GlobalRateLimiting =>
        rateLimiting.rateLimiter.limitExceeded(request.headers, request.cookies).map { limitExceeded =>
          if (limitExceeded) Xor.Left(TooManyRequests()) else Xor.right(request)
        }
      case _ => Future(Xor.right(request))
    }
  }

  def handleRequest(request: HttpRequest): Future[HttpResponse] = {
    val versionOpt: Option[String] = findVersion(request)
    val preparedRequest = removeVersionFromRequestIfPathVersioned(request, versionOpt)

    val matchedRoute = matchRequestToRoute(routes, preparedRequest)
    val pathVarMap = extractPathVariables(matchedRoute, request)

    val hulkHttpRequest = HulkHttpRequest(request.method, request.uri.path.toString(), request.headers, request.entity)(pathVarMap,
      request.uri.query(), request.uri.fragment)(request.cookies)

    val outgoingFilterResults = executeOutgoingFilters(filters, hulkHttpRequest)
    val incomingFilterResultOpt = findIncomingFilter(filters, outgoingFilterResults.size)

    val runIncomingFilterWithRequest = runIncomingFilter(hulkHttpRequest) _
    val response = incomingFilterResultOpt.flatMap(runIncomingFilterWithRequest)
      .getOrElse(runActionIfMatch(versionOpt, matchedRoute, hulkHttpRequest, request))

    outgoingFilterResults
      .foldLeft(response) { case (resp, filterResult) => resp.flatMap(filterResult) }
      .map(toAkkaHttpResponse => toAkkaHttpResponse)
  }

  private def runActionIfMatch(versionOpt: Option[String], matchedRoute: Option[(RouteDefWithRegex, Action)],
                               httpRequest: HulkHttpRequest, rawHttpRequest: HttpRequest): Future[HulkHttpResponse] = {
    matchedRoute.map { routeWithAction =>
      runAction(versionOpt, routeWithAction, httpRequest, rawHttpRequest)
    }.getOrElse(Action { req => NotFound() }.run(httpRequest).map(Future(_)).get)
  }

  private def runAction(versionOpt: Option[String], routeWithAction: (RouteDefWithRegex, Action),
                        httpRequest: HulkHttpRequest, rawHttpRequest: HttpRequest): Future[HulkHttpResponse] = {
    def runActionForWs(versionOpt: Option[String], action: WebSocketAction) = {
      val response = versionOpt.fold(action.run())(version => action.run(version))

      rawHttpRequest.header[UpgradeToWebsocket] match {
        case  Some(upgrade) =>
          response.map { case (sender, receiver) =>
            HulkHttpResponse.fromAkkaHttpResponse(upgrade.handleMessagesWithSinkSource(Sink.foreach(receiver), sender))
          }.getOrElse(Future(NotFound()))
        case _ => Future(NotFound())
      }
    }

    (versionOpt, routeWithAction._2) match {
      case (Some(version), action: SyncAction) => action.run(version, httpRequest).map(Future(_)).getOrElse(Future(NotFound()))
      case (None, action: SyncAction) => action.run(httpRequest).map(Future(_)).getOrElse(Future(NotFound()))
      case (Some(version), action: AsyncAction) => action.run(version, httpRequest).getOrElse(Future(NotFound()))
      case (None, action: AsyncAction) => action.run(httpRequest).getOrElse(Future(NotFound()))
      case (Some(version), action: WebSocketAction) => runActionForWs(Some(version), action)
      case (None, action: WebSocketAction) => runActionForWs(None, action)
      case (_, _) => throw new IllegalStateException("No appropriate action to run")
    }
  }

  private def removeVersionFromRequestIfPathVersioned(request: HttpRequest, versionOpt: Option[String]): HttpRequest = {
    val resultRequest =
      for(
        config <- hulkConfig;
        configVersioning <- config.versioning;
        version <- versionOpt
      ) yield {
        configVersioning match {
          case p: PathVersioning => request.copy(uri = request.uri.copy(path = request.uri.path.dropChars(version.length + 1)))
          case _ => request
        }
      }

    resultRequest.getOrElse(request)
  }

  private def findVersion(request: HttpRequest): Option[String] = {
    hulkConfig.flatMap { config =>
      config.versioning.flatMap { c => c match {
        case p: PathVersioning => Some(request.uri.path.toString().drop(1).takeWhile(_ != '/'))
        case a: AcceptHeaderVersioning => request.headers.find(_.name() == "Accept").flatMap(h => a.versionRegex.r.findFirstIn(h.value()))
        case av: AcceptVersionHeaderVersioning => request.headers.find(_.name() == "AcceptVersion").map(_.value())
        case _ => throw new IllegalArgumentException("Not a valid versioning strategy")
      }}
    }
  }

  private def extractPathVariables(matchedRoute: Option[(RouteDefWithRegex, Action)], request: HttpRequest): Map[String, String] = {
    matchedRoute.map { case (routeDef, action) =>
      routeDef.pathVarNames.map { pathVarName =>
        Try(routeDef.path.r(routeDef.pathVarNames: _*).findFirstMatchIn(request.uri.path.toString()).map(_.group(pathVarName)))
          .toOption.flatten
          .map(m => Some(pathVarName -> m))
          .getOrElse(None)
      }.filter(_.isDefined).map(_.get).toMap
    }.getOrElse(Map.empty)
  }

  private def matchRequestToRoute(routes: Map[RouteDefWithRegex, Action], request: HttpRequest): Option[(RouteDefWithRegex, Action)] = {
    def matchAnyMethod(routeDef: RouteDefWithRegex) = routeDef.method.isEmpty
    def matchAnyUri(routeDef: RouteDefWithRegex) = routeDef.path.isEmpty

    val method = request.method
    val path = request.uri.path

    val routesWithMatchingMethod = routes.filter { case (routeDef, _) => routeDef.method.contains(method) || matchAnyMethod(routeDef) }

    routesWithMatchingMethod.find { case (routeDef, action) =>
      path.toString().matches(routeDef.path)
    }.map { case (routeDef, action) =>
      (routeDef, action)
    }
  }

  private def findIncomingFilter(filtersSeq: Seq[Filter], outgoingFilterResultsSize: Int): Option[Filter] =
    filtersSeq.lift(outgoingFilterResultsSize)

  private def runIncomingFilter(httpRequest: HulkHttpRequest)(filter: Filter): Option[Future[HulkHttpResponse]] = {
    filter.filter(r => Future(r))(httpRequest).result.swap.toOption
  }

  private def executeOutgoingFilters(filters: Seq[Filter], httpRequest: HulkHttpRequest) = {
    def rec(filters: Seq[Filter], httpRequest: HulkHttpRequest): Seq[HulkHttpResponse => Future[HulkHttpResponse]] =
      filters.headOption.map(f => {
        val filterResult = f.filter(r => Future(r))(httpRequest)

        filterResult.result.fold(
          response => Seq.empty,
          responseFunc => {
            val returnSeq = rec(filters.tail, httpRequest)
            returnSeq :+ responseFunc
          }
        )
      }).getOrElse(Seq.empty)

    rec(filters, httpRequest).reverse
  }
}
