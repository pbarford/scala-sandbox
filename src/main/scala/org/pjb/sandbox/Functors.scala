package org.pjb.sandbox

object Functors extends App {
  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }
  implicit val listFunctor: Functor[List] = new Functor[List] {
    def map[A, B](fa: List[A])(f: A => B): List[B] = fa.map(f)
  }

  object Functor {
    def apply[F[_]](implicit f: Functor[F]) = f
  }

  val add = (x: Int, y: Int) => x + y
  val inc = add.curried(1)

  val b = Functor[List].map(List(1, 2, 3))(inc)

  //def inc(list: List[Int])(implicit func: Functor[List]) = func.map(list)(_ + 1)
  //val b = inc(List(1, 2, 3))
  println(b)
}
