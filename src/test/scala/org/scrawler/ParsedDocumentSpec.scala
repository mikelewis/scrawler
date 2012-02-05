package org.scrawler
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import akka.testkit.TestActorRef
import org.specs2.specification.Scope
import com.ning.http.client.HttpResponseBodyPart
import com.ning.http.client.Response
import com.ning.http.client.HttpResponseHeaders
import akka.actor.ActorRef

@RunWith(classOf[JUnitRunner])
class ParsedDocumentSpec extends Specification {
  TestServer.start

  class BaseBefore(crawlConfig: CrawlConfig = CrawlConfig()) extends Scope {
    def actorRef = TestActorRef(new UrlWorker(crawlConfig))
    def actor = actorRef.underlyingActor
  }

  "ParsedDocument" should {
    "contain all links found on page" in new BaseBefore() {
      val result = actor.fetchHtml("http://localhost:8910/links.html")
      val parsedDoc = result.left.get
      parsedDoc.urls.length must beEqualTo(15)
    }

    "contain body" in new BaseBefore() {
      val result = actor.fetchHtml("http://localhost:8910/links.html")
      val parsedDoc = result.left.get
      parsedDoc.body must beEqualTo(parsedDoc.response.getResponseBody())
    }

    "contain status code" in new BaseBefore() {
      val result = actor.fetchHtml("http://localhost:8910/links.html")
      val parsedDoc = result.left.get
      parsedDoc.statusCode must beEqualTo(200)
    }

    "contain headers" in new BaseBefore() {
      val result = actor.fetchHtml("http://localhost:8910/links.html")
      val parsedDoc = result.left.get
      parsedDoc.headers("Content-Type") must beEqualTo("text/html")
    }
  }

}