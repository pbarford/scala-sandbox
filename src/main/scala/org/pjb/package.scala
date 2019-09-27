package org

package object pjb {

  implicit class FunctionOperators[X,Y,Z](fn1: X => Y) {
    def ~> (fn2: Y => Z ): X => Z = fn1.andThen(fn2)
    def -> (fn2: Y => Z ): X => Z = fn1.andThen(fn2)
    def map(fn2 : Y => Z): X => Z = fn2.compose(fn1)
  }
}
