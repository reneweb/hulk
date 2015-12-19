package coast.http

import akka.http.scaladsl.model.{RequestEntity, HttpHeader, Uri, HttpMethod}

/**
  * Created by reweber on 18/12/2015
  */
case class CoastHttpRequest(method: HttpMethod, uri: Uri, httpHeader: Seq[HttpHeader], body: RequestEntity)
