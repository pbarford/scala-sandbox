package org.pjb.sandbox

import org.pjb.sandbox.Functors.Functor

object Applicatives extends App {

  trait Applicative[F[_]] extends Functor[F] {
    def pure[A](a: => A): F[A]
    def <*>[A,B](fa: => F[A])(f: => F[A => B]): F[B]
  }

  implicit val listApplicative: Applicative[List] =
    new Applicative[List] {
      def pure[A](a: => A): List[A] = List(a)

      def <*>[A,B](fa: => List[A])(f: => List[A => B]): List[B] = for {
        elem <- fa
        func <- f
      } yield func(elem)

      // we can reimplement map as <*> where f is wrapped around a list
      def map[A, B](fa: List[A])(f: A => B): List[B] = <*>(fa)(pure(f))
    }

  object Applicative {
    def apply[F[_]](implicit a: Applicative[F]) = a
  }


  val add = (x: Int, y: Int) => x + y

  val list1 = List(1, 2, 3)
  val list2 = List(4, 5, 6)

  val x = Applicative[List].<*>(list2)(Functor[List].map(list1)(add.curried))

  println(x)

  val af = Applicative[List]

  val lab: List[Int => String] = List((x: Int) => x.toString)
  val lbc: List[String => Int] = List((x: String) => x.length)
  val list = List(1, 2, 3)

  val comp = (bc: String => Int) => (ab: Int => String) => bc compose ab

  val res1 = af.<*>( af.<*>(list)(lab) )(lbc)
  val res2 = af.<*>(list)( af.<*>(lab)( af.<*>(lbc)( af.pure(comp) ) ) )

  println(res1)
  println(res2)
}
