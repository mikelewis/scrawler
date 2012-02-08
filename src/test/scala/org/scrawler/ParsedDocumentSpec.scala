package org.scrawler
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ParsedDocumentSpec extends RunTestServer {
  var (actorRef, actor) = testUrlWorker()

  describe("ParsedDocument") {
    it("should contain all links found on page") {
      val result = actor.fetchHtml(getUrl("links.html"))
      val parsedDoc = result.left.get
      parsedDoc.urls should have size (15)
    }

    it("should contain body") {
      val result = actor.fetchHtml(getUrl("links.html"))
      val parsedDoc = result.left.get
      parsedDoc.body should be(parsedDoc.response.getResponseBody())
    }

    it("contain status code") {
      val result = actor.fetchHtml(getUrl("links.html"))
      val parsedDoc = result.left.get
      parsedDoc.statusCode should be(200)
    }

    it("should contain headers") {
      val result = actor.fetchHtml(getUrl("links.html"))
      val parsedDoc = result.left.get
      parsedDoc.headers("Content-Type") should be("text/html")
    }
  }
}