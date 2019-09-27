package org.pjb.model.utils

import org.pjb.model.version.{Empty, Populated, Versioned}

object VersionedUtils {

  implicit class OptionOps[A](op:Option[A]) {
    def toVersioned(ver:Long):Versioned[A] = {
      op match {
        case Some(v) => Populated(ver, v)
        case None => Empty
      }
    }
  }
}
