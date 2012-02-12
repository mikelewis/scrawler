package org.scrawler
import akka.actor.Actor.actorOf
import akka.util.duration._
import scala.util.matching.Regex
import akka.dispatch._
import akka.actor.PoisonPill

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
    var resultFuture = processor.?(StartCrawl(url))(timeout = crawlConfig.timeout seconds)

    // My god this is messy, but I couldn't find a way to make it more idiomatic :/
    val value =
      try {
        try {
          resultFuture.get
        } catch {
          case ex => {
            try {
              processor.?(StopCrawl)(timeout = 10 seconds).get
            } catch {
              case _ => Logger.error(Crawl, "Tried to gracefully shutdown, but failed."); List[String]()
            }
          }
        }
      } finally {
        shutdownCrawler
      }

    value

    // resultFuture.onComplete { _ =>
    //  shutdownCrawler
    // }

    /* resultFuture.onTimeout { _ =>
      println("TIMED OUT FUCKKK")
      resultFuture = processor.?(StopCrawl)(timeout = 60 seconds)
      resultFuture.onComplete { _ =>
        println("COMPLETE FROM TIMEOUT")
      	shutdownCrawler
      }
    }*/

    //resultFuture.mapTo[List[String]]
  }
}