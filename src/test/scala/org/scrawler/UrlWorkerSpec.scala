package org.scrawler
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UrlWorkerSpec extends RunTestServer {
  var (actorRef, actor) = testUrlWorker()

  describe("processUrl") {
    it("should return DoneUrl on success") {
      actor.processUrl(getUrl("basic.html")).isInstanceOf[DoneUrl] should be(true)
    }

    it("should return DoneUrl on failure") {
      actor.processUrl("http://localasdfasdf:324/asdf").isInstanceOf[DoneUrl] should be(true)
    }

    it("should return DoneUrl which has url and FinalDocument") {
     //actor.processUrl(getUrl("basic.html"))
    }
  }
}