package coast.http

import akka.http.scaladsl.model._

/**
  * Created by reweber on 18/12/2015
  */
case class CoastHttpResponse(statusCode: StatusCode, httpHeader: Seq[HttpHeader], body: RequestEntity)
