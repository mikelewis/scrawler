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

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class GeneralUtilsSpec extends Specification {
  TestServer.start

  class BaseBefore(crawlConfig: CrawlConfig = CrawlConfig()) extends Scope {
    def actorRef = TestActorRef(new UrlWorker(crawlConfig))
    def actor = actorRef.underlyingActor
  }

  "genericRegexMatch" should {
    "return true for a given match" in {
      GeneralUtils.genericRegexMatch(Seq("""brains""".r, """hey""".r), "13hey432") must beTrue
    }

    "return false for a failed match" in {
      GeneralUtils.genericRegexMatch(Seq("""brains""".r, """hey""".r), "13432") must beFalse
    }
  }

  "getHeadersFromResponse" should {
    "return a map of headers" in new BaseBefore() {
      val result = actor.fetchHtml("http://localhost:8910/links.html")
      val parsedDoc = result.left.get
      parsedDoc.response.getHeaders.clear
      parsedDoc.response.getHeaders.put("test", List("value"))
      parsedDoc.response.getHeaders.put("test2", List("value1"))
      parsedDoc.response.getHeaders.put("test3", List("value1", "value2"))
      val mapHeader = GeneralUtils.getHeadersFromResponse(parsedDoc.response)
      mapHeader.size must beEqualTo(3)
      mapHeader("test") must beEqualTo("value")
      mapHeader("test2") must beEqualTo("value1")
      mapHeader("test3") must beEqualTo("value1,value2")

    }
  }
}