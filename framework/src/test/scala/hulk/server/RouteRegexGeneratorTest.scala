package hulk.server

import akka.http.scaladsl.model.HttpMethods
import hulk.http.{Action, Ok}
import hulk.routing.{*, RouteDef, Router}
import org.specs2.mutable.Specification

import scala.concurrent.Future

/**
  * Created by reweber on 07/02/2016
  */
class RouteRegexGeneratorTest extends Specification {

  val action = Action(request => Future.successful(Ok()))

  "RouteRegexGenerator#generateRoutesWithRegex" should {

    "create route from router" >> {
      val router = new Router() {
        override def router: Map[RouteDef, Action] = Map(
          (HttpMethods.GET, "/route") -> action
        )
      }

      val routeWithRegex = new RouteRegexGenerator(router).generateRoutesWithRegex()
      routeWithRegex must haveSize(1)
      routeWithRegex.toSeq.head._1.method.get must equalTo(HttpMethods.GET)
      routeWithRegex.toSeq.head._1.path must equalTo("/route")
      routeWithRegex.toSeq.head._1.pathVarNames must beEmpty
    }


    "create route with regular expression for path params from router" >> {
      val router = new Router() {
        override def router: Map[RouteDef, Action] = Map(
          (HttpMethods.GET, "/route/:{myPathParam}") -> action
        )
      }

      val routeWithRegex = new RouteRegexGenerator(router).generateRoutesWithRegex()
      routeWithRegex must haveSize(1)
      routeWithRegex.toSeq.head._1.method.get must equalTo(HttpMethods.GET)
      routeWithRegex.toSeq.head._1.path must equalTo("/route/(?<myPathParam>[^/]+)")
      routeWithRegex.toSeq.head._1.pathVarNames.head must equalTo("myPathParam")
    }

    "create route with custom regular expression from router if custom one given" >> {
      val router = new Router() {
        override def router: Map[RouteDef, Action] = Map(
          (HttpMethods.GET, "/route/:{matchAllParam : .*}") -> action
        )
      }

      val routeWithRegex = new RouteRegexGenerator(router).generateRoutesWithRegex()
      routeWithRegex must haveSize(1)
      routeWithRegex.toSeq.head._1.method.get must equalTo(HttpMethods.GET)
      routeWithRegex.toSeq.head._1.path must equalTo("/route/(?<matchAllParam>.*)")
      routeWithRegex.toSeq.head._1.pathVarNames.head must equalTo("matchAllParam")
    }

    "create route with no specified HTTP method if wildcard is used for HTTP method" >> {
      val router = new Router() {
        override def router: Map[RouteDef, Action] = Map(
          (*, "/route") -> action
        )
      }

      val routeWithRegex = new RouteRegexGenerator(router).generateRoutesWithRegex()
      routeWithRegex must haveSize(1)
      routeWithRegex.toSeq.head._1.method must beNone
      routeWithRegex.toSeq.head._1.path must equalTo("/route")
      routeWithRegex.toSeq.head._1.pathVarNames must beEmpty
    }
  }
}
