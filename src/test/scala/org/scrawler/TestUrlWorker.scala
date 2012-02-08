package org.scrawler
import akka.testkit.TestActorRef


class TestUrlWorker(crawlConfig: CrawlConfig = CrawlConfig()) {
  def actorRef = TestActorRef(new UrlWorker(crawlConfig))
  def actor = actorRef.underlyingActor

}