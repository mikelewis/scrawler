package org.scrawler
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.EitherValues._
import org.scalatest.BeforeAndAfterEach

@RunWith(classOf[JUnitRunner])
class UrlWorkerSpec extends RunTestServer with TestHooks with BeforeAndAfterEach {
  var (actorRef, actor) = testUrlWorker()

  override def beforeEach() {
    var (actorRef, actor) = testUrlWorker()
  }

  describe("processUrl") {
    it("should return DoneUrl on success") {
      actor.processUrl(getUrl("basic.html")).isInstanceOf[DoneUrl] should be(true)
    }

    it("should return DoneUrl on failure") {
      actor.processUrl("http://localasdfasdf:324/asdf").isInstanceOf[DoneUrl] should be(true)
    }

    it("should return DoneUrl which has url and Final Document") {
      actor.processUrl(getUrl("basic.html")) match {
        case DoneUrl(url, doc) =>
          url should be(getUrl("basic.html"))
          doc.isInstanceOf[FinalDocument] should be(true)
        case _ => fail()
      }
    }

    it("should return DoneUrl which has ParsedDocument for success") {
      actor.processUrl(getUrl("basic.html")) match {
        case DoneUrl(url, doc) =>
          doc.isInstanceOf[ParsedDocument] should be(true)
        case _ => fail()
      }
    }

    it("should return DoneUrl which has FailedDocument for failure") {
      actor.processUrl("http://localhsoi3:302/hi.html") match {
        case DoneUrl(url, doc) =>
          doc.isInstanceOf[FailedDocument] should be(true)
        case _ => fail()
      }
    }
  }

  describe("fetchHtml") {
    it("shouuld return FailedDocument on invalid url") {
      actor.fetchHtml("http://somethingthatdoesnotexistriteguyz.com").right.value.isInstanceOf[FailedDocument] should be(true)
    }

    it("shouuld return FailedDocument on successful request") {
      actor.fetchHtml(getUrl("basic.html")).left.value.isInstanceOf[ParsedDocument] should be(true)
    }

    it("shouuld return FailedDocument on non-200 response") {
      actor.fetchHtml(getUrl("404.html")).left.value.isInstanceOf[ParsedDocument] should be(true)
    }
  }

  describe("hooks") {
    it("should abort request when header hook returns false") {
      var (actorRef, actor) = testUrlWorker(CrawlConfig(hooks = TestHeaderHookFail))
      val result = actor.fetchHtml(getUrl("basic.html"))
      result.right.value.isInstanceOf[AbortedDocumentDuringHeaders] should be(true)
    }

    it("should continue request when header hook returns true") {
      var (actorRef, actor) = testUrlWorker(CrawlConfig(hooks = TestHeaderHookPass))
      val result = actor.fetchHtml(getUrl("basic.html"))
      result should be('left)
    }

    it("should abort request when status hook returns false") {
      var (actorRef, actor) = testUrlWorker(CrawlConfig(hooks = TestStatusHookFail))
      val result = actor.fetchHtml(getUrl("basic.html"))
      result.right.value.isInstanceOf[AbortedDocumentDuringStatus] should be(true)
    }

    it("continue request when status hook returns true") {
      var (actorRef, actor) = testUrlWorker(CrawlConfig(hooks = TestHeaderHookPass))
      val result = actor.fetchHtml(getUrl("basic.html"))
      result should be('left)
    }

    it("abort request when bodypart hook returns false") {
      var (actorRef, actor) = testUrlWorker(CrawlConfig(hooks = TestBodyPartHookFail))
      val result = actor.fetchHtml(getUrl("long.html"))
      result.left.value.body.length should be < 146170
    }

    it("continue request when bodypart hook returns true") {
      var (actorRef, actor) = testUrlWorker(CrawlConfig(hooks = TestBodyPartHookPass))
      val result = actor.fetchHtml(getUrl("long.html"))
      result.left.value.body.length should be(146170)
    }
  }
}