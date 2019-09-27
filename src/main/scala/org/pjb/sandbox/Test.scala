package org.pjb.sandbox

object Test extends App {

  implicit class VersionedFooOps(thisFoo:Versioned[Foo]) {
    def mergeType(otherFoo: Versioned[Foo]): Versioned[Foo] = (thisFoo, otherFoo) match {
      case (Populated(v1, d1), Populated(v2, d2)) =>
        Populated(if(v2 > v1) v2 else v1,
                  Foo(d1.a.merge(d2.a), d1.b.merge(d2.b)))
      case (Empty, Populated(_, _)) => otherFoo
      case _ => thisFoo
    }
    def changesInType(ver:Long): Versioned[Foo] = thisFoo match {
      case Populated(v1, d1) if v1 >= ver => Populated(v1, Foo(d1.a.changes(ver), d1.b.changes(ver)))
      case _ => Empty
    }
  }


  trait Ops[A <: Ops[A]] { this: A =>
    def merge(input: A) :A
    def changes(ver:Long): A
  }

  case class Blah(a:Versioned[String], b:Versioned[Int], foo:Versioned[Foo]) extends Ops[Blah] {
    override def merge(input: Blah): Blah =
      Blah(a.merge(input.a),
           b.merge(input.b),
           foo.mergeType(input.foo))

    override def changes(ver: Long): Blah =
      Blah(a.changes(ver), b.changes(ver), foo.changesInType(ver))
  }

  case class Foo(a:Versioned[String] = Empty, b:Versioned[Boolean] = Empty)

  val state = Blah(Empty, Empty, Empty)
  val update1 = Blah(Populated(1, "test1"), Empty, Populated(1, Foo(Populated(1, "foo1"), Populated(1, true))))
  val update2 = Blah(Empty, Populated(2, 3423), Empty)
  val update3 = Blah(Populated(3, "test1"), Empty, Populated(3, Foo(Populated(3, "foo3"))))

  val t1 = state.merge(update1)
  println(t1)
  println(t1.changes(1))
  val t2 = t1.merge(update2)
  println(t2)
  println(t2.changes(2))
  val t3 = t2.merge(update3)
  println(t3)
  println(t3.changes(1))
  println(t3.changes(2))
  println(t3.changes(3))

  case class Bar(a:Ver[Int] = Emp, b:Ver[Int] = Emp)

  val b1 = Bar()
  val b2 = Bar(b1.a update 3, b1.b update 54)
  val b3 = Bar(b2.a update 5, b2.b update 54)
  println(b2)
  println(b3)


  ex(println("heelo"))

  def ex[R](block: => R):R = {
    block
  }



}
