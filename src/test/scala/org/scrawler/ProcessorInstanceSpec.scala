package org.scrawler
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ProcessorInstanceSpec extends MasterSuite {
  var (actorRef, actor) = testProcessor()

  describe("sanitizeurl") {
    it("should return none if invalid url") {
      actor.validateAndSanitizeUrl("htt://invalidurl") should be(None)
    }

    it("should url if valid url") {
      actor.validateAndSanitizeUrl("http://google.com") should be(Some("http://google.com/"))
    }

    it("should return sanitized url") {
      actor.validateAndSanitizeUrl("http://google.com#test") should be(Some("http://google.com"))
    }
  }
}