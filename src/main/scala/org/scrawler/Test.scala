package org.scrawler
import akka.actor.Actor.actorOf
import akka.actor.PoisonPill
import com.ning.http.client.Response
import com.ning.http.client.HttpResponseBodyPart

class SampleCallbacks extends Callbacks {
  override def proccessedUrl(url: String) {
    //Logger.info(self, "Got url! " + url)
  }
}

object SampleHooks extends Hooks {
  override def canContinueFromBodyPartReceived(response: Response, bodyPart: HttpResponseBodyPart) = {
    Logger.info(SampleHooks, "Size of " + response.getUri().toString + " is " + response.getResponseBody.length)
    response.getResponseBody().length < 10000
  }
}

object Test extends App {
  //Crawl("http://leafo.net/", CrawlConfig(maxDepth = 1))
  val callbackActor = actorOf[SampleCallbacks].start()
  val future = Crawl.site("http://leafo.net/lessphp/docs/index.html", CrawlConfig(maxDepth = 0, callbacks = callbackActor, hooks = SampleHooks))
  future.get
  callbackActor ! PoisonPill
  println("DONE!")
}