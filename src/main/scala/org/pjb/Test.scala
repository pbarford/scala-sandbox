package org.pjb

object Test extends App {

  case class Blah(i:Int)

  val x = t1 ~> t2 ~> print
  val y = t1.map(t2.map(print))
  val z = t1 -> t2 -> print

  println(t1.map(b => b.i+3434)(34))

  x(38748)
  y(433)
  z(49878)

  def t1 : Int => Blah = {
    i => Blah(i + 10)
  }

  def t2 : Blah => String = {
    s => s"SSSS:${s.i}"
  }

  def print : String => Unit = {
    s => println(s)
  }

  def quicksort: List[Int] => List[Int] = {
    case Nil => Nil
    case h::Nil => List(h)
    case pivot::tail =>
      val (less,greater) = tail.partition(_ < pivot)
      println(s"pivot=$pivot less=$less greater=$greater")
      quicksort(less) ::: (pivot :: quicksort(greater))
  }

  val l = List(4,7,2,6,2,5,9,8)
  println(List(1, 2) ::: List(3, 4))
  println(quicksort(l))

}
