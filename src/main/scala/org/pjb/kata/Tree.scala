package org.pjb.kata

import org.pjb.kata.Tree.NodeEnum.NodeType
import pprint._

import scala.annotation.tailrec

object Tree extends App {
  object NodeEnum extends Enumeration {
    type NodeType = Value
    val RIGHT, LEFT = Value
  }

  case class Tree(key:Int, left:Option[Tree] = None, right:Option[Tree] = None) {
    self =>

    private def balanceFactor:Int = {
      @tailrec
      def go(count:Int, tree:Option[Tree], fn: Tree => Option[Tree]):Int =
        tree match {
          case Some(t) =>
            go(count+1, fn(t), fn)
          case None => count
        }
      val lh = go(0, self.left, t => t.left)
      val rh = go(0, self.right, t=> t.right)
      //println(s"balanceFactor [${self.value}] LH=$lh RH=$rh BF=${rh - lh}")
      rh - lh
    }

    private def balanceRequired:Boolean = {
      def go(child: Option[Tree]): Boolean = {
        child match {
          case Some(c) =>
            val bf = c.balanceFactor
            if (bf > 1 || bf < -1) true else false
          case None => false
        }
      }
      go(left) || go(right)
    }

    private def rebalance:Tree = {
      def checkAndRotate(c:Tree):Tree = {
        c.balanceFactor match {
          case x if x < -1 => c.rotateRight
          case x if x > 1 => c.rotateLeft
          case _ => c
        }
      }

      def go(child:Option[Tree], nodeType:NodeType):Option[Tree] = {
        child match {
          case Some(c) =>
            val n = checkAndRotate(c)
            (n.left, n.right) match {
              case (Some(_), Some(_)) =>
                Some(n.copy(left = go(n.left, NodeEnum.LEFT), right = go(n.right, NodeEnum.RIGHT)))
              case (Some(_), None) =>
                Some(n.copy(left = go(n.left, NodeEnum.LEFT)))
              case (None, Some(_)) =>
                Some(n.copy(right = go(n.right, NodeEnum.RIGHT)))
              case _ => Some(n)
            }
          case None => None
        }
      }
      val u = checkAndRotate(self)
      var res = u.copy(left = go(u.left, NodeEnum.LEFT), right = go(u.right, NodeEnum.RIGHT))

      while(res.balanceRequired) {
        //println("re-balance again")
        res = res.copy(left = go(res.left, NodeEnum.LEFT), right = go(res.right, NodeEnum.RIGHT))
      }
      res
    }

    private def rotateRight:Tree = {
      //println(s"rotateRight [${self.value}]")
      self.left match {
        case None => self
        case Some(nodeB) =>
          nodeB.right match {
            case Some(rightChildNodeB) =>
              //println(s"[${self.value}] rightChildNodeB.rotateLeft ${rightChildNodeB.value}")
              val nna = self.copy(left = Some(rightChildNodeB.rotateLeft))
              nodeB.copy(right = Some(nna))
            case None =>
              val nna = self.copy(left = None)
              val nnbl = nodeB.left match {
                case Some(leftChildNodeB) =>
                  //println(s"[${self.value}] leftChildNodeB.rotateLeft ${leftChildNodeB.value}")
                  //Some(leftChildNodeB.rotateLeft)
                  Some(leftChildNodeB)
                case None => None
              }
              nodeB.copy(right = Some(nna), left = nnbl)
          }
      }
    }

    private def rotateLeft:Tree = {
      //println(s"rotateLeft [${self.value}]")
      self.right match {
        case None => self
        case Some(nodeB) =>
          nodeB.left match {
            case Some(leftChildNodeB) =>
              //println(s"[${self.value}] leftChildNodeB.rotateRight ${leftChildNodeB.value}")
              val nna = self.copy(right = Some(leftChildNodeB.rotateRight))
              nodeB.copy(left = Some(nna))
            case None =>
              val nna = self.copy(right = None)
              val nnb = nodeB.right match {
                case Some(rightChildNodeB) =>
                  //println(s"[${self.value}] rightChildNodeB.rotateRight ${rightChildNodeB.value}")
                  //Some(rightChildNodeB.rotateRight)
                  Some(rightChildNodeB)
                case None => None
              }
              nodeB.copy(left = Some(nna), right = nnb)
          }
      }
    }

    private def findLargestInTree(tree: Tree) : Tree = {
      tree match {
        case Tree(_, _, Some(rt)) => findLargestInTree(rt)
        case _ => tree
      }
    }

    private def findSmallestInTree(tree: Tree) : Tree = {
      tree match {
        case Tree(_, Some(lt), _) => findSmallestInTree(lt)
        case _ => tree
      }
    }

    def remove(key:Int):Tree = {
      def generateBranch(branch:Tree) : Option[Tree] = {
        branch match {
          case Tree(_, None, None) => None
          case Tree(_, None, Some(rt)) => Some(rt)
          case Tree(_, Some(lt), None) => Some(lt)
          case Tree(_, Some(lt), Some(rt)) =>
            val largestOnLeft = findLargestInTree(lt)
            val leftMinusLargest = go(largestOnLeft.key, lt)
            Some(Tree(largestOnLeft.key, leftMinusLargest, Some(rt)))
        }
      }
      def go(v:Int, branch:Tree): Option[Tree] = {
        if(branch.key == v) generateBranch(branch)
        else {
          if(v < branch.key) {
            if(branch.left.isDefined) Some(branch.copy(left = go(v, branch.left.get), right = branch.right))
            else Some(branch)
          } else {
            if (branch.right.isDefined) Some(branch.copy(left = branch.left, right = go(v, branch.right.get)))
            else Some(branch)
          }
        }
      }
      go(key, self).get.rebalance
    }

    def find(key:Int):Option[Tree] = {
      @tailrec
      def go(v:Int, branch:Option[Tree]): Option[Tree] = {
        branch match {
          case Some(b) => if(b.key == v) branch
          else if(b.key > v) go(v, b.left)
          else go(v, b.right)
          case None => None
        }
      }
      go(key, Some(self))
    }

    def add(v:Int):Tree = {
      def go(v:Int, branch:Tree):Tree = {
        branch match {
          case b@Tree(tv, Some(lt), Some(rt)) =>
            if(tv > v) b.copy(left = Some(go(v, lt)))
            else b.copy(right = Some(go(v, rt)))

          case b@Tree(tv, Some(lt), None) =>
            if(tv > v) b.copy(left = Some(go(v, lt)))
            else b.copy(right = Some(Tree(v)))

          case b@Tree(tv, None, Some(rt)) =>
            if(tv < v) b.copy(right = Some(go(v, rt)))
            else b.copy(left = Some(Tree(v)))

          case b@Tree(tv, None, None) =>
            if(tv > v) b.copy(left = Some(Tree(v)))
            else b.copy(right = Some(Tree(v)))
        }
      }
      go(v, self).rebalance
    }
  }

  val l = Tree(1).add(2).add(3).add(4).add(5).add(6)
  pprintln(l)
  //val r = Tree(6).add(5).add(4).add(3).add(2).add(1)
  //pprintln(r)

  val y = Tree(23).add(2).add(12).add(45).add(33).add(8).add(10).add(9).add(3).add(21).add(40).add(7).add(6).add(16).add(32).add(54).add(13)//.add(60)
  pprintln(y)
  val z = y.remove(8)
  pprintln(z)
  pprintln(z.add(18).add(22).add(8))


}

