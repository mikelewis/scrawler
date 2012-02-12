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

  describe("generateCanonicalQueryString") {
    it("should return an empty string if passing in None") {
      UrlUtils.generateCanonicalQueryString(None) should be("")
    }

    it("should handle a key without a value") {
      UrlUtils.generateCanonicalQueryString(Some("MA")) should be("MA")
    }

    it("should return correct query string") {
      UrlUtils.generateCanonicalQueryString(Some("flag=true&name=mike")) should be("flag=true&name=mike")
    }

    it("should return query string in lexical order") {
      UrlUtils.generateCanonicalQueryString(Some("name=mike&bat=3&log=true")) should be("bat=3&log=true&name=mike")
    }

    it("should handle empty value in (key,value) pair") {
      UrlUtils.generateCanonicalQueryString(Some("name=mike&bat=3&log=")) should be("bat=3&log=&name=mike")
    }

    it("should handle empty key in (key,value) pair") {
      UrlUtils.generateCanonicalQueryString(Some("name=mike&=3&log=true")) should be("=3&log=true&name=mike")
    }

    it("should encode str") {
      UrlUtils.generateCanonicalQueryString(Some("flag=true 3&name=mike@gmail")) should be("flag=true+3&name=mike%40gmail")
    }
  }

  describe("sanitizeUrl") {

    it("should accept url with query string") {
      val uri = new URI("http://google.com?name=3")
      UrlUtils.sanitizeUrl(uri).toString() should be("http://google.com?name=3")
    }

    it("should accept url with path(slash) and query string") {
      val uri = new URI("http://google.com/index/car/index.php/?name=3")
      UrlUtils.sanitizeUrl(uri).toString() should be("http://google.com/index/car/index.php/?name=3")
    }

    it("should accept url with path( non file) and query string") {
      val uri = new URI("http://google.com/index/car/index?name=3")
      UrlUtils.sanitizeUrl(uri).toString() should be("http://google.com/index/car/index?name=3")
    }

    it("should accept url with query string ( and slash )") {
      val uri = new URI("http://google.com/?name=3")
      UrlUtils.sanitizeUrl(uri).toString() should be("http://google.com/?name=3")
    }

    it("should accept url with no query string") {
      val uri = new URI("http://google.com/index.html")
      UrlUtils.sanitizeUrl(uri).toString() should be("http://google.com/index.html")
    }

    it("should accept https") {
      val uri = new URI("https://google.com/index.html")
      UrlUtils.sanitizeUrl(uri).toString() should be("https://google.com/index.html")
    }

    it("should strip fragments") {
      val uri = new URI("http://google.com#hi")
      UrlUtils.sanitizeUrl(uri).toString() should be("http://google.com")
    }

    it("should not strip out trailing slash") {
      val uri = new URI("http://google.com/")
      UrlUtils.sanitizeUrl(uri).toString() should not be ("http://google.com")
    }

    it("should strip out port 80") {
      val uri = new URI("http://google.com:80")
      UrlUtils.sanitizeUrl(uri).toString() should be("http://google.com")
    }

    it("should strip out blank port") {
      val uri = new URI("http://google.com:")
      UrlUtils.sanitizeUrl(uri).toString() should be("http://google.com")
    }

    it("should lowercase url") {
      val uri = new URI("HTTP://GoogLe.com")
      UrlUtils.sanitizeUrl(uri).toString() should be("http://google.com")
    }

    it("should keep unreserved chars") {
      val uri = new URI("http://www.example.com/%7Eusername?name=mike%2Dlewis&birth=united%2Estates&phone=555%5F555")
      UrlUtils.sanitizeUrl(uri)
        .toString() should be("http://www.example.com/~username?birth=united.states&name=mike-lewis&phone=555_555")
    }
  }

  describe("createURI") {
    it("Returns uri for valid uri") {
      UrlUtils.createURI("http://google.com") should be(Some(URI.create("http://google.com/")))
    }

    it("returns none for invalid uri") {
      UrlUtils.createURI("google.com") should be(None)
    }

    it("should return none for invalid host") {
      UrlUtils.createURI("http://searchgasm_example.binarylogic.com") should be(None)
    }
  }
}