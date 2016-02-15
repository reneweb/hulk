package hulk.http

import org.specs2.mutable.Specification

/**
  * Created by reweber on 15/02/2016
  */
class HulkHttpResponseTest extends Specification {

  "HulkHttpResponse#apply" should {
    "create 200 response if build from OK object" >> {
      val ok = Ok()
      ok.statusCode.intValue() must equalTo(200)
    }

    "create 201 response if build from Created object" >> {
      val created = Created()
      created.statusCode.intValue() must equalTo(201)
    }

    "create 202 response if build from Accepted object" >> {
      val accepted = Accepted()
      accepted.statusCode.intValue() must equalTo(202)
    }

    "create 203 response if build from NonAuthoritiveInformation object" >> {
      val nonAuthoritiveInformation = NonAuthoritiveInformation()
      nonAuthoritiveInformation.statusCode.intValue() must equalTo(203)
    }

    "create 204 response if build from NoContent object" >> {
      val noContent = NoContent()
      noContent.statusCode.intValue() must equalTo(204)
    }

    "create 205 response if build from ResetContent object" >> {
      val resetContent = ResetContent()
      resetContent.statusCode.intValue() must equalTo(205)
    }

    "create 206 response if build from PartialContent object" >> {
      val partialContent = PartialContent()
      partialContent.statusCode.intValue() must equalTo(206)
    }

    "create 300 response if build from MultipleChoices object" >> {
      val multipleChoices = MultipleChoices()
      multipleChoices.statusCode.intValue() must equalTo(300)
    }

    "create 301 response if build from MovedPermanently object" >> {
      val movedPermanently = MovedPermanently()
      movedPermanently.statusCode.intValue() must equalTo(301)
    }

    "create 302 response if build from Found object" >> {
      val found = Found()
      found.statusCode.intValue() must equalTo(302)
    }

    "create 303 response if build from SeeOther object" >> {
      val seeOther = SeeOther()
      seeOther.statusCode.intValue() must equalTo(303)
    }

    "create 304 response if build from NotModified object" >> {
      val notModified = NotModified()
      notModified.statusCode.intValue() must equalTo(304)
    }

    "create 305 response if build from UseProxy object" >> {
      val useProxy = UseProxy()
      useProxy.statusCode.intValue() must equalTo(305)
    }

    "create 307 response if build from TemporaryRedirect object" >> {
      val temporaryRedirect = TemporaryRedirect()
      temporaryRedirect.statusCode.intValue() must equalTo(307)
    }

    "create 400 response if build from BadRequest object" >> {
      val badRequest = BadRequest()
      badRequest.statusCode.intValue() must equalTo(400)
    }

    "create 401 response if build from Unauthorized object" >> {
      val unauthorized = Unauthorized()
      unauthorized.statusCode.intValue() must equalTo(401)
    }

    "create 402 response if build from PaymentRequired object" >> {
      val paymentRequired = PaymentRequired()
      paymentRequired.statusCode.intValue() must equalTo(402)
    }

    "create 403 response if build from Forbidden object" >> {
      val forbidden = Forbidden()
      forbidden.statusCode.intValue() must equalTo(403)
    }

    "create 404 response if build from NotFound object" >> {
      val notFound = NotFound()
      notFound.statusCode.intValue() must equalTo(404)
    }

    "create 405 response if build from MethodNotAllowed object" >> {
      val methodNotAllowed = MethodNotAllowed()
      methodNotAllowed.statusCode.intValue() must equalTo(405)
    }

    "create 406 response if build from NotAcceptable object" >> {
      val notAcceptable = NotAcceptable()
      notAcceptable.statusCode.intValue() must equalTo(406)
    }

    "create 407 response if build from ProxyAuthenticationRequired object" >> {
      val proxyAuthenticationRequired = ProxyAuthenticationRequired()
      proxyAuthenticationRequired.statusCode.intValue() must equalTo(407)
    }

    "create 408 response if build from RequestTimeout object" >> {
      val requestTimeout = RequestTimeout()
      requestTimeout.statusCode.intValue() must equalTo(408)
    }

    "create 409 response if build from Conflict object" >> {
      val conflict = Conflict()
      conflict.statusCode.intValue() must equalTo(409)
    }

    "create 410 response if build from Gone object" >> {
      val gone = Gone()
      gone.statusCode.intValue() must equalTo(410)
    }

    "create 411 response if build from LengthRequired object" >> {
      val lengthRequired = LengthRequired()
      lengthRequired.statusCode.intValue() must equalTo(411)
    }

    "create 412 response if build from PreconditionFailed object" >> {
      val preconditionFailed = PreconditionFailed()
      preconditionFailed.statusCode.intValue() must equalTo(412)
    }

    "create 413 response if build from HttpResponseBodyTooLarge object" >> {
      val httpResponseBodyTooLarge = HttpResponseBodyTooLarge()
      httpResponseBodyTooLarge.statusCode.intValue() must equalTo(413)
    }

    "create 414 response if build from RequestUriTooLong object" >> {
      val requestUriTooLong = RequestUriTooLong()
      requestUriTooLong.statusCode.intValue() must equalTo(414)
    }

    "create 415 response if build from UnsupportedMediaType object" >> {
      val unsupportedMediaType = UnsupportedMediaType()
      unsupportedMediaType.statusCode.intValue() must equalTo(415)
    }

    "create 416 response if build from RequestRangeNotSatisfiable object" >> {
      val requestRangeNotSatisfiable = RequestRangeNotSatisfiable()
      requestRangeNotSatisfiable.statusCode.intValue() must equalTo(416)
    }

    "create 417 response if build from ExpectationFailed object" >> {
      val expectationFailed = ExpectationFailed()
      expectationFailed.statusCode.intValue() must equalTo(417)
    }

    "create 428 response if build from PreconditionRequired object" >> {
      val preconditionRequired = PreconditionRequired()
      preconditionRequired.statusCode.intValue() must equalTo(428)
    }

    "create 429 response if build from TooManyRequests object" >> {
      val tooManyRequests = TooManyRequests()
      tooManyRequests.statusCode.intValue() must equalTo(429)
    }

    "create 431 response if build from NetworkAuthenticationRequired object" >> {
      val requestHeaderFieldsTooLarge = RequestHeaderFieldsTooLarge()
      requestHeaderFieldsTooLarge.statusCode.intValue() must equalTo(431)
    }

    "create 500 response if build from InternalServerError object" >> {
      val internalServerError = InternalServerError()
      internalServerError.statusCode.intValue() must equalTo(500)
    }

    "create 501 response if build from NotImplemented object" >> {
      val notImplemented = NotImplemented()
      notImplemented.statusCode.intValue() must equalTo(501)
    }

    "create 502 response if build from BadGateway object" >> {
      val badGateway = BadGateway()
      badGateway.statusCode.intValue() must equalTo(502)
    }

    "create 503 response if build from ServiceUnavailable object" >> {
      val serviceUnavailable = ServiceUnavailable()
      serviceUnavailable.statusCode.intValue() must equalTo(503)
    }

    "create 504 response if build from GatewayTimeout object" >> {
      val gatewayTimeout = GatewayTimeout()
      gatewayTimeout.statusCode.intValue() must equalTo(504)
    }

    "create 505 response if build from HttpVersionNotSupported object" >> {
      val httpVersionNotSupported = HttpVersionNotSupported()
      httpVersionNotSupported.statusCode.intValue() must equalTo(505)
    }

    "create 511 response if build from NetworkAuthenticationRequired object" >> {
      val networkAuthenticationRequired = NetworkAuthenticationRequired()
      networkAuthenticationRequired.statusCode.intValue() must equalTo(511)
    }
  }
}
