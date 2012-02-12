package org.scrawler
import akka.actor.Actor.actorOf
import akka.actor.PoisonPill
import akka.util.duration._
import com.ning.http.client.Response
import com.ning.http.client.HttpResponseBodyPart
import akka.dispatch.Future
import java.util.Date

class SampleCallbacks extends Callbacks {
  override def proccessedUrl(url: String) {
    //Logger.info(self, "Got url! " + url)
  }
}

object SampleHooks extends Hooks {
}

object Test extends App {
  val callbackActor = actorOf[SampleCallbacks].start()
  val startTime = new Date
  val urls = Crawl.site("http://leafo.net", CrawlConfig(callbacks = callbackActor, hooks = SampleHooks))

  println("GOT URLS! " + urls)

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