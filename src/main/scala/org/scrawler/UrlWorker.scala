package org.scrawler
import akka.actor.Actor
import akka.actor.PoisonPill
import scala.util.Random

class UrlWorker extends Actor {
  val randomGenerator = Random
 def receive = {
   case ProcessUrl(url) =>
     // something
     fetchHtml(url)
 } 
 
 def fetchHtml(url: String) {
   self.reply(DoneUrl(url, List("elem 1" + randomGenerator.nextInt.toString, 
       "elem 2" + randomGenerator.nextInt.toString)))
 }
}