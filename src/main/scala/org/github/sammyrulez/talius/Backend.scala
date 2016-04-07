package org.github.sammyrulez.talius

/**
  * Created by sam on 06/04/16.
  */
trait Backend[E,P] {

  def subscribe(event:E, f:(P => Unit)):Unit

  def map[R]( f:(E,(P => Unit)) => R ):Seq[R]

}
