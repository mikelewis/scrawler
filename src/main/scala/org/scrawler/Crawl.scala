package org.scrawler
import akka.actor.Actor.actorOf
import akka.util.duration._
import scala.util.matching.Regex
import akka.dispatch._
import akka.actor.PoisonPill
import akka.dispatch.Promise

object Crawl {
  def apply(url: String, crawlConfig: CrawlConfig = CrawlConfig()) = {
    (new Crawl(url, crawlConfig)).start()
  }

  def host(hostStr: String, secure: Boolean, crawlConfig: CrawlConfig = CrawlConfig()) = {
    val newConfig = crawlConfig.copy(hosts = (crawlConfig.hosts ++ List(hostStr.r)))
    val scheme = if (secure) "https://" else "http://"
    (new Crawl(scheme + hostStr, newConfig)).start()
  }

  def site(url: String, crawlConfig: CrawlConfig = CrawlConfig()) = {
    val host = new java.net.URI(url).getHost
    val newConfig = crawlConfig.copy(hosts = (crawlConfig.hosts ++ List(host.r)))
    (new Crawl(url, newConfig)).start()
  }
}

class Crawl(url: String, crawlConfig: CrawlConfig) {

  val processor = actorOf(new Processor(crawlConfig)).start()

  def shutdownCrawler {
    if (crawlConfig.callbacks.actorClass == classOf[DefaultCallbacks])
      crawlConfig.callbacks ! PoisonPill

    processor.stop
    Logger.shutdownLogger
  }

  def start() = {
    try {
      val promise = Promise[List[String]](scala.Int.MaxValue)
      promise completeWith {
        processor.?(StartCrawl(url))(timeout = crawlConfig.timeout seconds).mapTo[List[String]].
          onTimeout { _ =>
            promise.completeWithResult(processor.?(StopCrawl)(timeout = 10 seconds).mapTo[List[String]].get)
          }
      }

      promise.get

    } finally {
      shutdownCrawler
    }
  }
}