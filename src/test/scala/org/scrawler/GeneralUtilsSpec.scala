package org.scrawler
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class GeneralUtilsSpec extends RunTestServer {
  var (actorRef, actor) = testUrlWorker()

  describe("genericRegexMatic") {
    it("should return true for a given match") {
      GeneralUtils.genericRegexMatch(Seq("""brains""".r, """hey""".r), "13hey432") should be(true)
    }

    it("return false for a failed match") {
      GeneralUtils.genericRegexMatch(Seq("""brains""".r, """hey""".r), "13432") should be(false)
    }
  }

  describe("getHeadersFromResponse") {
    it("should return a map of headers") {
      val result = actor.fetchHtml(getUrl("links.html"))
      val parsedDoc = result.left.get
      parsedDoc.response.getHeaders.clear
      parsedDoc.response.getHeaders.put("test", List("value"))
      parsedDoc.response.getHeaders.put("test2", List("value1"))
      parsedDoc.response.getHeaders.put("test3", List("value1", "value2"))
      val mapHeader = GeneralUtils.getHeadersFromResponse(parsedDoc.response)
      mapHeader.size should be(3)
      mapHeader should (contain key("test") and contain key("test2"))
      mapHeader("test") should be("value")
      mapHeader("test2") should be("value1")
      mapHeader("test3") should be("value1,value2")
    }
  }
}