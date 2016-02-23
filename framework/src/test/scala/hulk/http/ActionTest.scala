package hulk.http

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

      val action = Action.apply(request => Ok())
      val response = action.run(request).get

      response.statusCode.intValue() must equalTo(200)
    }

    "run action of specified version" >> {
      val request = mock[HulkHttpRequest]

      val action = Action.apply( "v1" -> {request: HulkHttpRequest => Ok()} )
      val response = action.run("v1", request).get

      response.statusCode.intValue() must equalTo(200)
    }

    "return none if running action with non existent version" >> {
      val request = mock[HulkHttpRequest]

      val action = Action.apply( "v1" -> {request: HulkHttpRequest => Ok()} )
      val responseOpt = action.run("v2", request)

      responseOpt must be(None)
    }
  }

  "AsyncAction#run" should {
    "run action" >> {
      val request = mock[HulkHttpRequest]

      val action = AsyncAction.apply(request => Future(Ok()))
      val response = Await.result(action.run(request).get, 5 seconds)

      response.statusCode.intValue() must equalTo(200)
    }

    "run action of specified version" >> {
      val request = mock[HulkHttpRequest]

      val action = AsyncAction.apply( "v1" -> {request: HulkHttpRequest => Future(Ok())} )
      val response = Await.result(action.run("v1", request).get, 5 seconds)

      response.statusCode.intValue() must equalTo(200)
    }

    "return none if running action with non existent version" >> {
      val request = mock[HulkHttpRequest]

      val action = AsyncAction.apply( "v1" -> {request: HulkHttpRequest => Future(Ok())} )
      val responseOpt = action.run("v2", request)

      responseOpt must be(None)
    }
  }
}
