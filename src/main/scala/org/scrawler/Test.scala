package org.scrawler
import akka.actor.Actor.actorOf
import akka.actor.PoisonPill

class SampleCallbacks extends Callbacks {
  override def proccessedUrl(url: String) {
    Logger.info(self, "Got url! " + url)
  }
}

object Test extends App {
  //Crawl("http://leafo.net/", CrawlConfig(maxDepth = 1))
  val callbackActor = actorOf[SampleCallbacks].start()
  val future = Crawl.host("leafo.net", false, CrawlConfig(maxDepth = 99, callbacks = callbackActor))
  future.get
  callbackActor ! PoisonPill
  println("DONE!")
}