package org.pjb.sandbox

trait Ops[A <: Ops[A]] { this: A =>
  def merge(input: A) :A
  def changes(ver:Long): A
}

sealed trait Ver[+A] {
  def update[T >: A](input: T): Ver[T]
}

case object Emp extends Ver[Nothing] {
  override def update[T >: Nothing](input: T): Ver[T] = Pop(1, input)
}

case class Pop[A](ver:Long, value:A) extends Ver[A] {
  override def update[T >: A](input: T): Ver[T] = {
    if(value != input) Pop(ver + 1, input) else Pop(ver, value)
  }
}


sealed trait Versioned[+A] { this: Versioned[A] =>
  def merge[T >: Versioned[A]](input: T) :T
  def changes[T >: Versioned[A]](ver:Long): T
}

case object Empty extends Versioned[Nothing] {
  override def merge[T >: Versioned[Nothing]](input: T): T = input
  override def changes[T >: Versioned[Nothing]](ver: Long): T = Empty
}

case class Populated[V](ver:Long, value:V) extends Versioned[V] {
  override def merge[T >: Versioned[V]](input: T): T = input match {
    case Populated(newVer, newValue) if newVer > this.ver && newValue != this.value => input
    case _ => this
  }

  override def changes[T >: Versioned[V]](ver: Long): T = {
    if(this.ver == ver) this else Empty
  }
}
