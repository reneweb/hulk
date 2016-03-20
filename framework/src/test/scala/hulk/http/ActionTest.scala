package hulk.http

import akka.http.scaladsl.model.ws.{TextMessage, Message}
import akka.stream.scaladsl.Source
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 07/02/2016
  */
class ActionTest extends Specification with Mockito {

  "Action#run" should {
    "run action" >> {
      val request = mock[HulkHttpRequest]

      val action = Action.apply(request => Future(Ok()))
      val response = Await.result(action.run(request).get, 5 seconds)

      response.statusCode.intValue() must equalTo(200)
    }

    "run action of specified version" >> {
      val request = mock[HulkHttpRequest]

      val action = Action.apply( "v1" -> {request: HulkHttpRequest => Future(Ok())} )
      val response = Await.result(action.run("v1", request).get, 5 seconds)

      response.statusCode.intValue() must equalTo(200)
    }

    "return none if running action with non existent version" >> {
      val request = mock[HulkHttpRequest]

      val action = Action.apply( "v1" -> {request: HulkHttpRequest => Future(Ok())} )
      val responseOpt = action.run("v2", request)

      responseOpt must be(None)
    }
  }

  "WebSocketAction#run" should {
    "run action" >> {
      val source = Source.single( TextMessage(""))
      val func = (msg: Message) => {}

      val action = WebSocketAction.apply(source, func)
      val resultOpt = action.run()

      resultOpt must beSome

      val (sourceRes, funcRes) = resultOpt.get
      sourceRes must equalTo(source)
      funcRes must equalTo(funcRes)
    }

    "run action of specified version" >> {
      val source = Source.single( TextMessage(""))
      val func = (msg: Message) => {}

      val action = WebSocketAction.apply("v1" -> (source, func))
      val resultOpt = action.run("v1")

      resultOpt must beSome

      val (sourceRes, funcRes) = resultOpt.get
      sourceRes must equalTo(source)
      funcRes must equalTo(funcRes)
    }

    "return none if running action with non existent version" >> {
      val source = Source.single( TextMessage(""))
      val func = (msg: Message) => {}

      val action = WebSocketAction.apply("v1" -> (source, func))
      val resultOpt = action.run("v2")

      resultOpt must beNone
    }
  }
}
