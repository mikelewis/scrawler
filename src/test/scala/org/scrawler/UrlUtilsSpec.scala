package org.scrawler
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.net.URI

@RunWith(classOf[JUnitRunner])
class UrlUtilsSpec extends MasterSuite {
  describe("isValidUrl") {
    it("should return true for a valid url") {
      UrlUtils.isValidUrl("http://yahoo.com") should be(true)
    }

    it("should return false for an invalid url") {
      UrlUtils.isValidUrl("nohttp.com") should be(false)
    }
  }

  describe("getHost") {
    it("should return host for a valid uri") {
      val uri = new URI("http://google.com")
      UrlUtils.getHost(uri) should be("google.com")
    }
  }

  describe("sanitizeUrl") {
    it("should strip fragments") {
      val uri = new URI("http://google.com#hi")
      UrlUtils.sanitizeUrl(uri).toString() should be("http://google.com")
    }
  }

  describe("createURI") {
    it("Returns uri for valid uri") {
      UrlUtils.createURI("http://google.com") should be(Some(URI.create("http://google.com/")))
    }

    it("returns none for invalid uri") {
      UrlUtils.createURI("google.com") should be(None)
    }
  }
}