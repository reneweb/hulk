package coast.http

import akka.stream.scaladsl.Flow

/**
  * Created by reweber on 18/12/2015
  */
class Action[A](f: (CoastHttpRequest => Flow[CoastHttpRequest, CoastHttpResponse, A])) {

  def run(coastHttpRequest: CoastHttpRequest): Flow[CoastHttpRequest, CoastHttpResponse, A] = {
    f(coastHttpRequest)
  }
}

object Action {
  def apply(f: CoastHttpRequest => Flow[CoastHttpRequest, CoastHttpResponse, Any]) = new Action(f)
}
