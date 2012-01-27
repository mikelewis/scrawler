package org.scrawler
import akka.actor.Actor.actorOf
import akka.util.duration._
import scala.util.matching.Regex

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

  def start(): List[String] = {
    val future = processor.?(StartCrawl(url))(timeout = 300 seconds)

    future.get match {
      case urls: List[String] => println("Finished! Got " + urls.size + " urls\n\n\n\n\n" + urls)
      case _ => println("Something went wrong")
    }

    processor.stop
    Logger.shutdownLogger
    List("saf")
  }
}