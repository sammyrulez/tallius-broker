package org.github.sammyrulez.talius


import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.forkjoin._

sealed trait BrokerOption {}

case class Async() extends BrokerOption

case class Sync() extends BrokerOption

case class Repeating(p: FiniteDuration) extends BrokerOption

class Broker[E, P](backend: Backend[E, P], transformers: List[(E, P => P)] = List()) {
  def subscribe(event: E, f: (P => Unit)): Unit = backend.subscribe(event, f)

  def publish(event: E, payload: P, option: BrokerOption = new Sync()): Unit = {

    val eventTransformers: List[(E, (P) => P)] = transformers.filter(_._1.equals(event)) :+ ((event,identity[P](_)))
    val finalPayload: P = eventTransformers.foldRight(payload)((element, current) => element._2(current))

    import ExecutionContext.Implicits.global
    option match {
      case Async() => {
        Future({
          applyEvent(event, finalPayload)
        })
      }
      case Sync() => applyEvent(event, finalPayload)
      case Repeating(d) => {
        DelayedFuture(d)({
          applyEvent(event, finalPayload)
          publish(event, payload)}
        )
      }
    }
  }

  private def applyEvent(event: E, finalPayload: P): Seq[Unit] = {
    backend.map((e, c) => {
      event match {
        case e => c(finalPayload)
      }
    })
  }
}
