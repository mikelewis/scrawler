package org.scrawler
import akka.actor.Actor.actorOf
import akka.util.duration._

/*
 * TODO: Use CrawlConfig (and merge in hosts if they call Crawl.site/ Craw.host)
 * merge being passedInConfig.copy(hosts = ...)
 */
object Crawl {
  def apply(url: String, crawlConfig: CrawlConfig = CrawlConfig()) = {
    (new Crawl(url, crawlConfig)).start()
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
    List("saf")
  }
}