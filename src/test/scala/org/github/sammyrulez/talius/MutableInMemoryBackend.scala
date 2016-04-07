package org.github.sammyrulez.talius

/**
  * Created by sam on 07/04/16.
  */
class MutableInMemoryBackend[E,P] extends Backend[E,P] {

  var subscribers:List[((E, (P) => Unit))] = List()

  override def subscribe(event: E, f: (P) => Unit): Unit = {
    subscribers = subscribers :+  (event,f)
  }

  override def map[R](f: (E, (P) => Unit) => R): Seq[R] = {
    subscribers.map(t => f(t._1,t._2))
  }
}
