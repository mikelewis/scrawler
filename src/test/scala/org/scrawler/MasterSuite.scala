package org.scrawler
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.OneInstancePerTest
import akka.testkit.TestActorRef


class MasterSuite extends FunSpec with ShouldMatchers {
  class TestUrlWorker(crawlConfig: CrawlConfig = CrawlConfig()) {
    def actorRef = TestActorRef(new UrlWorker(crawlConfig))
    def actor = actorRef.underlyingActor
  }
  
  def testUrlWorker(crawlConfig: CrawlConfig = CrawlConfig()) = {
    val actorRef = TestActorRef(new UrlWorker(crawlConfig))
    (actorRef, actorRef.underlyingActor)
  }
}