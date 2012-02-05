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

object TestHeaderHookFail extends Hooks {
  override def canContinueFromHeaders(response: Response, headers: Map[String, String]) = {
    false
  }
}

object TestHeaderHookPass extends Hooks {
  override def canContinueFromHeaders(response: Response, headers: Map[String, String]) = {
    true
  }
}

object TestStatusHookFail extends Hooks {
  override def canContinueFromStatusCode(response: Response, status: Integer) = {
    false
  }
}

object TestStatusHookPass extends Hooks {
  override def canContinueFromStatusCode(response: Response, status: Integer) = {
    true
  }
}

object TestBodyPartHookFail extends Hooks {
  override def canContinueFromBodyPartReceived(response: Response, part: HttpResponseBodyPart) = {
    response.getResponseBody().length < 1193
  }
}

object TestBodyPartHookPass extends Hooks {
  override def canContinueFromBodyPartReceived(response: Response, part: HttpResponseBodyPart) = {
    true
  }
}

@RunWith(classOf[JUnitRunner])
class UrlWorkerSpec extends Specification {
  TestServer.start

  class BaseBefore(crawlConfig: CrawlConfig = CrawlConfig()) extends Scope {
    def actorRef = TestActorRef(new UrlWorker(crawlConfig))
    def actor = actorRef.underlyingActor
  }

  "fetchHtml" should {
    "return FailedDocument on invalid url" in new BaseBefore {
      actor.fetchHtml("http://somethingthatdoesnotexistriteguyz.com") must beRight.like {
        case x: FailedDocument => ok
        case _ => ko
      }
    }

    "return Left(ParsedDocument) for a successful request" in new BaseBefore {
      actor.fetchHtml("http://localhost:8910/basic.html") must beLeft.like {
        case x: ParsedDocument => ok
        case _ => ko
      }
      
      "return Left(ParsedDocument) for a non-200 response" in new BaseBefore {
        actor.fetchHtml("http://localhost:8910/404.html") must beLeft.like {
          case x : ParsedDocument => x.statusCode must beEqualTo(404)
          case _ => ko
        }
      }
    }

  }

  "hooks" should {
    "abort request when header hook returns false" in new BaseBefore(CrawlConfig(hooks = TestHeaderHookFail)) {
      val result = actor.fetchHtml("http://localhost:8910/basic.html")
      result.fold(x => ko, y => y match { case a: AbortedDocumentDuringHeaders => ok; case _ => ko })
    }

    "continue request when header hook returns true" in new BaseBefore(CrawlConfig(hooks = TestHeaderHookPass)) {
      val result = actor.fetchHtml("http://localhost:8910/basic.html")
      result.fold(x => ok, _ => ko)
    }

    "abort request when status hook returns false" in new BaseBefore(CrawlConfig(hooks = TestStatusHookFail)) {
      val result = actor.fetchHtml("http://localhost:8910/basic.html")
      result.fold(x => ko, y => y match { case a: AbortedDocumentDuringStatus => ok; case _ => ko })
    }

    "continue request when status hook returns true" in new BaseBefore(CrawlConfig(hooks = TestStatusHookPass)) {
      val result = actor.fetchHtml("http://localhost:8910/basic.html")
      result.fold(x => ok, _ => ko)
    }

    "abort request when bodypart hook returns false" in new BaseBefore(CrawlConfig(hooks = TestBodyPartHookFail)) {
      val result = actor.fetchHtml("http://localhost:8910/long.html")
      result.fold(x => x.body.length must beLessThan(146170), _ => ko)
    }

    "continue request when bodypart hook returns true" in new BaseBefore(CrawlConfig(hooks = TestBodyPartHookPass)) {
      val result = actor.fetchHtml("http://localhost:8910/long.html")
      result.fold(x => x.body.length must be equalTo (146170), _ => ko)
    }
  }

}