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
import akka.testkit.TestKit
import scala.collection.JavaConversions._
import akka.actor.Actor

@RunWith(classOf[JUnitRunner])
class ProcessorSpec extends Specification with TestKit {
  class BaseBefore(crawlConfig: CrawlConfig = CrawlConfig()) extends Scope {
    def actorRef = TestActorRef(new Processor(crawlConfig))
    def actor = actorRef.underlyingActor
  }

  class BeforeActor(crawlConfig: CrawlConfig = CrawlConfig()) extends Scope {
    val actor = Actor.actorOf(new Processor(crawlConfig))
  }

  TestServer.start

  "integration tests" should {
    "not get into cycle" in new BeforeActor(CrawlConfig(maxDepth = 2)) {
      actor.start()
      val result = actor ? StartCrawl("http://localhost:8910/cycle.html")
      result.get must beEqualTo(List("http://localhost:8910/cycle.html", "http://localhost:8910/cycle1.html"))
    }

    "default maxDepth is infinite" in new BeforeActor {
      actor.start()
      val result = actor ? StartCrawl("http://localhost:8910/cycle.html")
      result.get must beEqualTo(List("http://localhost:8910/cycle.html", "http://localhost:8910/cycle1.html"))

    }

    "not process invalid links" in new BeforeActor {
      actor.start()
      val result = actor ? StartCrawl("http://localhost:8910/invalid_links.html")
      result.get.asInstanceOf[List[String]] must haveTheSameElementsAs(List("http://somethingthatdoesntexist-butisval1d.com", "http://localhost:8910/invalid_links.html"))
    }

    "maxDepth rule handle 0" in new BeforeActor(CrawlConfig(maxDepth = 0)) {
      actor.start()
      val result = actor ? StartCrawl("http://localhost:8910/invalid_links.html")
      result.get.asInstanceOf[List[String]] must beEqualTo(List("http://localhost:8910/invalid_links.html"))
    }

    "maxDepth rule of 1" in new BeforeActor(CrawlConfig(maxDepth = 1)) {
      actor.start()
      val result = actor ? StartCrawl("http://localhost:8910/1.html")
      result.get.asInstanceOf[List[String]] must haveTheSameElementsAs(List("http://localhost:8910/3.html", "http://localhost:8910/2.html", "http://localhost:8910/1.html"))
    }

    // last one, I swear :)
    "maxDepth rule of 2" in new BeforeActor(CrawlConfig(maxDepth = 2)) {
      actor.start()
      val result = actor ? StartCrawl("http://localhost:8910/1.html")
      result.get.asInstanceOf[List[String]] must haveTheSameElementsAs(List("http://localhost:8910/7.html", "http://localhost:8910/6.html", "http://localhost:8910/5.html", "http://localhost:8910/4.html", "http://localhost:8910/3.html", "http://localhost:8910/2.html", "http://localhost:8910/1.html"))
    }
  }

  "finishedWithCurrentDepth" should {
    "return true if currentlyProcessing is empty" in new BaseBefore {
      actor.finishedWithCurrentDepth must beTrue
    }
  }

  "isFinished" should {
    "return true if queuedUrls size = 0" in new BaseBefore(CrawlConfig(maxDepth = 0)) {
      actor.isFinished must beTrue
    }
  }

  "sanitizeurl" should {
    "return none if invalid url" in new BaseBefore {
      actor.validateAndSanitizeUrl("htt://invalidurl") must beNone
    }

    "return url if valid url" in new BaseBefore {
      actor.validateAndSanitizeUrl("http://google.com") must beSome.like {
        case x: String => x must beEqualTo("http://google.com/")
        case _ => ko
      }
    }

    "return sanitized url" in new BaseBefore {
      actor.validateAndSanitizeUrl("http://google.com#test") must beSome.like {
        case x: String => x must beEqualTo("http://google.com")
        case _ => ko
      }
    }
  }

  "empty queue" should {
    "return empty queue" in new BaseBefore {
      actor.emptyQueue.size must beEqualTo(0)
    }
  }
}