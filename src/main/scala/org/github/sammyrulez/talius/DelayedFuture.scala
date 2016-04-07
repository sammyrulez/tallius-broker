package org.github.sammyrulez.talius

object DelayedFuture {
  import java.util.{Timer, TimerTask}
  import scala.concurrent._
  import scala.concurrent.duration.FiniteDuration
  import scala.util.Try

  private val timer = new Timer

  private def makeTask[T]( body: => T )( schedule: TimerTask => Unit )(implicit ctx: ExecutionContext): Future[T] = {
    val prom = Promise[T]()
    schedule(
      new TimerTask{
        def run() {
          ctx.execute(
            new Runnable {
              def run() {
                prom.complete(Try(body))
              }
            }
          )
        }
      }
    )
    prom.future
  }

  def apply[T]( duration: FiniteDuration )( body: => T )(implicit ctx: ExecutionContext): Future[T] = {
    makeTask( body )( timer.schedule( _, duration.toMillis ) )
  }
}
