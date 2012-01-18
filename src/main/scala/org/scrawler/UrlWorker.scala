package org.scrawler
import akka.actor.Actor
import akka.actor.PoisonPill


sealed trait WorkType
case class StartCrawl(url: String) extends WorkType
case class ProcessUrl(url: String) extends WorkType
case class DoneUrl(url: String, urls: List[String]) extends WorkType

class UrlWorker extends Actor {
 def receive = {
   case ProcessUrl(url) =>
     // something
     fetchHtml(url)
 } 
 
 def fetchHtml(url: String) {
   self.reply(DoneUrl(url, List("Poop", "poop 1")))
 }
}