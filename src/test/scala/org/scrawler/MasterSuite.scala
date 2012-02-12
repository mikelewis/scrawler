package org.scrawler
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.OneInstancePerTest
import akka.testkit.TestActorRef
import akka.actor.Actor
import com.ning.http.client.AsyncHttpClient

class MasterSuite extends FunSpec with ShouldMatchers {
  class TestUrlWorker(crawlConfig: CrawlConfig = CrawlConfig()) {
    def actorRef = TestActorRef(new UrlWorker(new AsyncHttpClient(crawlConfig.httpClientConfig), crawlConfig))
    def actor = actorRef.underlyingActor
  }

  def testUrlWorker(crawlConfig: CrawlConfig = CrawlConfig()) = {
    val actorRef = TestActorRef(new UrlWorker(new AsyncHttpClient(crawlConfig.httpClientConfig), crawlConfig))
    (actorRef, actorRef.underlyingActor)
  }

  def testProcessor(crawlConfig: CrawlConfig = CrawlConfig()) = {
    val actorRef = TestActorRef(new Processor(crawlConfig))
    (actorRef, actorRef.underlyingActor)
  }

  def testActualProcessor(crawlConfig: CrawlConfig = CrawlConfig()) = {
    Actor.actorOf(new Processor(crawlConfig))
  }
}