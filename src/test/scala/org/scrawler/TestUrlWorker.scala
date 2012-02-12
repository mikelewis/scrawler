package org.scrawler
import akka.testkit.TestActorRef
import com.ning.http.client.AsyncHttpClient


class TestUrlWorker(crawlConfig: CrawlConfig = CrawlConfig()) {
  def actorRef = TestActorRef(new UrlWorker(new AsyncHttpClient(crawlConfig.httpClientConfig), crawlConfig))
  def actor = actorRef.underlyingActor

}