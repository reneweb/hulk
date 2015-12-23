package coast.http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by reweber on 18/12/2015
  */
class Action(f: Either[((CoastHttpRequest, ActorSystem, ActorMaterializer) => CoastHttpResponse),
                       ((CoastHttpRequest, ActorSystem, ActorMaterializer) => Future[CoastHttpResponse])]) {

  def run(coastHttpRequest: CoastHttpRequest)(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer): Future[CoastHttpResponse] = {
    f match {
      case Left(nonFutureF) => Future(nonFutureF(coastHttpRequest, actorSystem, actorMaterializer))
      case Right(futureF) => futureF(coastHttpRequest, actorSystem, actorMaterializer)
    }
  }
}

object Action {
  def apply(f: CoastHttpRequest => CoastHttpResponse) = {
    val funcWithAkkaSetup = (c: CoastHttpRequest, as: ActorSystem, am: ActorMaterializer) => f(c)
    new Action(Left(funcWithAkkaSetup))
  }

  def apply(f: (CoastHttpRequest, ActorSystem, ActorMaterializer) => CoastHttpResponse) = new Action(Left(f))
}

object AsyncAction {
  def apply(f: CoastHttpRequest => Future[CoastHttpResponse]) = {
    val funcWithAkkaSetup = (c: CoastHttpRequest, as: ActorSystem, am: ActorMaterializer) => f(c)
    new Action(Right(funcWithAkkaSetup))
  }

  def apply(f: (CoastHttpRequest, ActorSystem, ActorMaterializer) => Future[CoastHttpResponse]) = new Action(Right(f))
}