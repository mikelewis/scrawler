/*
 * TODO WORK IN PROGRESS
 */

package org.scrawler
import scala.util.matching.Regex

class Robots(userAgent: String) {
  val disallows = scala.collection.mutable.ListBuffer[Regex]()
  val allows = scala.collection.mutable.ListBuffer[Regex]()

  def isAllowed(url: String): Boolean = {
    (allows.exists { rule =>
      url match {
        case rule() => true
        case _ => false
      }
    }
      ||
      !disallows.exists { rule =>
        url match {
          case rule() => true
          case _ => false
        }
      })
  }
}