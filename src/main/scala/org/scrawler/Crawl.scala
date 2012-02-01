package org.scrawler
import akka.actor.Actor.actorOf
import akka.util.duration._
import scala.util.matching.Regex
import akka.dispatch._
import akka.actor.PoisonPill

/*
 * TODO: Use CrawlConfig (and merge in hosts if they call Crawl.site/ Craw.host)
 * merge being passedInConfig.copy(hosts = ...)
 */
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

  // List of strings will probably change
  def start(): Future[List[String]] = {
    val future = processor.?(StartCrawl(url))(timeout = 300 seconds)
    future.onComplete { _ =>

      if (crawlConfig.callbacks.actorClass == classOf[DefaultCallbacks])
        crawlConfig.callbacks ! PoisonPill

      processor.stop
      Logger.shutdownLogger
    }

    future.mapTo[List[String]]
  }
}