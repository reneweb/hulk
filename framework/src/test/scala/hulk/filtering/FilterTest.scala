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
    override def filter(next: Next): (HulkHttpRequest) => FilterResult = {
      case req: HulkHttpRequest => Future(NotFound())
    }
  }

  class TestOutgoingFilter extends Filter {
    override def filter(next: Next): (HulkHttpRequest) => FilterResult = {
      case req: HulkHttpRequest => next andThen { response => Future(NotFound()) }
    }
  }

  class TestDontFilter extends Filter {
    override def filter(next: Next): (HulkHttpRequest) => FilterResult = {
      case req: HulkHttpRequest => next
    }
  }

  "Filter#filter" should {
    "filter request and pass direct response if request is matching filter condition and response given" >> {
      def nextF(resp: HulkHttpResponse) =  Future(resp)
      val filterResult = new TestIncomingFilter().filter(nextF).apply(mock[HulkHttpRequest])
      val response = Await.result(filterResult.result.swap.toOption.get, 2 seconds)
      response.statusCode.intValue() must equalTo(404)
    }

    "filter request, but pass through action if request is matching filter condition and req => resp func is given" >> {
      def nextF(resp: HulkHttpResponse) =  Future(resp)
      val filterResult = new TestOutgoingFilter().filter(nextF).apply(mock[HulkHttpRequest])
      val response = Await.result(filterResult.result.toOption.get.apply(Ok()), 2 seconds)
      response.statusCode.intValue() must equalTo(404)
    }

    "not filter request if not matching filter condition" >> {
      def nextF(resp: HulkHttpResponse) =  Future(resp)
      val filterResult = new TestDontFilter().filter(nextF).apply(mock[HulkHttpRequest])
      val response = Await.result(filterResult.result.toOption.get.apply(Ok()), 2 seconds)
      response.statusCode.intValue() must equalTo(200)
    }
  }
}
