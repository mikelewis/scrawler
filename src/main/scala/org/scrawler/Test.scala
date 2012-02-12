package org.scrawler
import akka.actor.Actor.actorOf
import akka.actor.PoisonPill
import akka.util.duration._
import com.ning.http.client.Response
import com.ning.http.client.HttpResponseBodyPart
import akka.dispatch.Future

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
  val callbackActor = actorOf[SampleCallbacks].start()
  val urls = Crawl.site("http://leafo.net", CrawlConfig(timeout=1, callbacks = callbackActor, hooks = SampleHooks))
  println("GOT URLS! " + urls)
  
 val jurls = Crawl.site("http://jouhanallende.com", CrawlConfig(timeout=1, callbacks = callbackActor, hooks = SampleHooks))
 println("GOT URLS! " + jurls)

  //  val leafFuture = Future {
  //    Crawl.site("http://leafo.net", CrawlConfig(maxDepth = 4, callbacks = callbackActor, hooks = SampleHooks))
  //  }
  //  
  //  val jouhanFuture = Future {
  //     Crawl.site("http://jouhanallende.com", CrawlConfig(maxDepth = 3, callbacks = callbackActor, hooks = SampleHooks))
  //  }
  //  
  //  val urls = for {
  //    a <- leafFuture.mapTo[List[String]].await(20 second)
  //    b <- jouhanFuture.mapTo[List[String]].await(20 second)
  //  } yield a ++ b
  //  
  //  
  //  println("Got urls!" + urls.await(20 second))
  callbackActor ! PoisonPill
  println("DONE!")
}