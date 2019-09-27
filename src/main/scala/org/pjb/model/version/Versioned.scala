package org.pjb.model.version

import org.pjb.model.version.Versioned.Ver

object Versioned {
  type Ver = Long
}

sealed trait Versioned[+A] { this: Versioned[A] =>
  def merge[T >: Versioned[A]](input: T) :T
  def diffSince[T >: Versioned[A]](ver:Ver): T
}

case object Empty extends Versioned[Nothing] {
  override def merge[T >: Versioned[Nothing]](input: T): T = input
  override def diffSince[T >: Versioned[Nothing]](ver: Ver): T = Empty
}

case class Populated[V](ver:Ver, value:V) extends Versioned[V] {
  override def merge[T >: Versioned[V]](input: T): T = input match {
    case Populated(newVer, newValue) if newVer > this.ver && newValue != this.value => input
    case _ => this
  }

  override def diffSince[T >: Versioned[V]](ver: Ver): T = {
    if(this.ver >= ver) this else Empty
  }
}
