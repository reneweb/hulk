package hulk.http

import akka.http.scaladsl.model.ws.{TextMessage, Message}
import akka.stream.scaladsl.Source
import hulk.filtering.Filter
import hulk.filtering.Filter.Next
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
    "give source and sender func" >> {
      val source = Source.single( TextMessage(""))
      val func = (msg: Message) => {}

      val action = WebSocketAction.apply(source, func)
      val resultOpt = action.run()

      resultOpt must beSome

      val (filters, sourceRes, funcRes) = resultOpt.get
      filters must beEmpty
      sourceRes must equalTo(source)
      funcRes must equalTo(funcRes)
    }

    "give source and sender func of specified version" >> {
      val source = Source.single( TextMessage(""))
      val func = (msg: Message) => {}

      val action = WebSocketAction.apply("v1" -> (source, func))
      val resultOpt = action.run("v1")

      resultOpt must beSome

      val (filters, sourceRes, funcRes) = resultOpt.get
      filters must beEmpty
      sourceRes must equalTo(source)
      funcRes must equalTo(funcRes)
    }

    "return none if calling action with non existent version" >> {
      val source = Source.single( TextMessage(""))
      val func = (msg: Message) => {}

      val action = WebSocketAction.apply("v1" -> (source, func))
      val resultOpt = action.run("v2")

      resultOpt must beNone
    }

    "give filters, source and sender func if filter is set " >> {
      val source = Source.single( TextMessage(""))
      val func = (msg: Message) => {}
      val filter = new Filter {
        override def apply(next: Next): (HulkHttpRequest) => Future[HulkHttpResponse] = {
          case req => Future.successful(Unauthorized())
        }
      }

      val action = WebSocketAction(Seq(filter), source, func)
      val resultOpt = action.run()

      resultOpt must beSome

      val (filters, sourceRes, funcRes) = resultOpt.get
      filters must haveLength(1)
      sourceRes must equalTo(source)
      funcRes must equalTo(funcRes)
    }
  }
}
