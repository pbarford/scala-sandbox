package org.pjb.kata

import scala.collection.SortedSet

object CoinMachine extends App {

  implicit val myOrdering: Ordering[Int] = Ordering.fromLessThan[Int](_ > _)
  val coins = Set(5,1,2,6,7,20,10,50)
  val orderedCoins = SortedSet.empty[Int] ++ coins

  assertDenominations(34)
  assertDenominations(18)
  assertDenominations(13)
  assertDenominations(8)
  assertDenominations(7)
  assertDenominations(23)

  println(getSmallestDenominations(34))
  println(getSmallestDenominations(18))
  println(getSmallestDenominations(13))
  println(getSmallestDenominations(8))
  println(getSmallestDenominations(7))
  println(getSmallestDenominations(23))


  def assertDenominations(amount: Int):Unit = {
    val denominations = determineDenominationsFor(amount)
    determineDenominationsFor(amount).foreach(l => assert(l.sum == amount))
    println(s"all [${denominations.size}] denominations for amount[$amount] are good")
  }

  def getSmallestDenominations(amount:Int):List[List[Int]] = {
    determineDenominationsFor(amount).foldLeft(List.empty[List[Int]]){
      (acc, cd) =>
        if(acc.isEmpty || acc.head.size > cd.size) List(cd)
        else if(acc.head.size == cd.size) cd :: acc
        else acc
    }
  }

  def filterCoinsGreaterThan(amount:Int): Set[Int] = {
    orderedCoins.filter(greaterThan(amount))
  }

  def greaterThan(a:Int) : Int => Boolean = i => i <= a

  def determineDenominationsFor(expectedTotal:Int): List[List[Int]] = {

    def go(runningTotal:List[Int], coins:List[Int]): List[List[Int]] = {
      coins match {
        case Nil => List.empty
        case h :: t =>
          val newTotal = processCoin(h, runningTotal)
          if(newTotal.sum == expectedTotal) List(newTotal)
          else if(h + newTotal.sum <= expectedTotal)  go(newTotal, h :: t) ++ go(newTotal, t)
          else go(newTotal, t)
      }
    }

    def processCoin(coin: Int, runningTotal:List[Int]): List[Int] = {
      if(runningTotal.sum + coin <= expectedTotal) runningTotal ++ List[Int](coin)
      else runningTotal
    }

    val coins = filterCoinsGreaterThan(expectedTotal).toList
    coins.flatMap(c => go(List.empty, c :: coins.filter(_ < c)))
  }

}
