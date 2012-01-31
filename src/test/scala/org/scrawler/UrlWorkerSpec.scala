package org.scrawler
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import akka.testkit.TestActorRef

import org.specs2.specification.{ Before, After, Step, Around }
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class UrlWorkerSpec extends Specification {
  trait before extends Scope {
    val crawlConfig = CrawlConfig()
    val actorRef = TestActorRef(new UrlWorker(crawlConfig))
    val actor = actorRef.underlyingActor
  }
  
  "fetchHtml" should {
    "should return FailedDocument on invalid url" in new before {
      actor.fetchHtml("http://somethingthatdoesnotexistriteguyz.com") must beRight
    }
  }

}