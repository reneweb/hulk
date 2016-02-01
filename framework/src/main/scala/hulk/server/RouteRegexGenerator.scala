package hulk.server

import akka.http.scaladsl.model.HttpMethod
import hulk.http.Action
import hulk.routing.Router

/**
  * Created by reweber on 01/02/2016
  */
class RouteRegexGenerator(router: Router) {

  def generateRoutesWithRegex(): Map[RouteDefWithRegex, Action] = {
    router.router.map{ case (routeDef, action) =>
      val pathVarNames = ":\\{[^}]*\\}".r.findAllIn(routeDef.path).toList.map(_.drop(2).dropRight(1)).map(_.split(":")(0).trim)
      val routeWithRegex = ":\\{[^}]*\\}".r.replaceAllIn(routeDef.path, r => {
        val name = r.toString().drop(2).dropRight(1).split(":")(0)
        val actualRegex = r.toString().drop(2).dropRight(1).split(":").lift(1).map(_.trim).getOrElse("[^/]+")
        s"(?<$name>$actualRegex)"
      })

      (RouteDefWithRegex(routeDef.method, routeWithRegex, pathVarNames), action)
    }
  }
}

case class RouteDefWithRegex(method: Option[HttpMethod], path: String, pathVarNames: Seq[String])
