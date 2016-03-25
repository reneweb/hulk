package hulk.filtering

import hulk.filtering.Filter._
import hulk.http.{HulkHttpRequest, HulkHttpResponse, NotFound, Ok}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 05/02/2016
  */
class FilterTest extends Specification with Mockito {

  class TestIncomingFilter extends Filter {
    override def apply(next: Next): (HulkHttpRequest) => Future[HulkHttpResponse] = {
      case req: HulkHttpRequest => Future(NotFound())
    }
  }

  class TestOutgoingFilter extends Filter {
    override def apply(next: Next): (HulkHttpRequest) => Future[HulkHttpResponse] = {
      case req: HulkHttpRequest => next andThen { response => Future(NotFound()) } apply req
    }
  }

  class TestDontFilter extends Filter {
    override def apply(next: Next): (HulkHttpRequest) => Future[HulkHttpResponse] = {
      case req: HulkHttpRequest => next(req)
    }
  }

  "Filter#filter" should {
    "filter request and pass direct response if request is matching filter condition and response given" >> {
      def nextF(req: HulkHttpRequest) =  Future(Ok())
      val filterResult = new TestIncomingFilter().apply(nextF).apply(mock[HulkHttpRequest])
      val response = Await.result(filterResult, 2 seconds)
      response.statusCode.intValue() must equalTo(404)
    }

    "filter request, but pass through action if request is matching filter condition and req => resp func is given" >> {
      def nextF(req: HulkHttpRequest) =  Future(Ok())
      val filterResult = new TestOutgoingFilter().apply(nextF).apply(mock[HulkHttpRequest])
      val response = Await.result(filterResult, 2 seconds)
      response.statusCode.intValue() must equalTo(404)
    }

    "not filter request if not matching filter condition" >> {
      def nextF(req: HulkHttpRequest) =  Future(Ok())
      val filterResult = new TestDontFilter().apply(nextF).apply(mock[HulkHttpRequest])
      val response = Await.result(filterResult, 2 seconds)
      response.statusCode.intValue() must equalTo(200)
    }
  }
}
