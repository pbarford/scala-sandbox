package org.pjb.sandbox

import scala.language.higherKinds

object HigherOrderTypes extends App {

  trait Foldable[F[_]] {
    def leftFold[A, B](ob: F[A])(zero: B)(fn: (B, A) => B): B
  }

  implicit val listFolable: Foldable[List] = new Foldable[List] {
    def leftFold[A, B](ob: List[A])(zero: B)(fn: (B, A) => B): B =
      ob.foldLeft(zero)(fn)
  }

  val res = List(1, 2, 3).leftFold(0)(_ + _)

  import scala.language.implicitConversions
  implicit class ListFoldableOpt[A](list: List[A])(implicit fold: Foldable[List]) {
    def leftFold[B](zero: B)(fn: (B, A) => B): B =
      fold.leftFold(list)(zero)(fn)
  }



  trait Foo[A, B] {
    def map[F[_], A, B](f: F[A])(fn: A => B): F[B]
  }
}
