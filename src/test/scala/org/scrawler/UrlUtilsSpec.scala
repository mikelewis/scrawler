package org.scrawler
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.net.URI

@RunWith(classOf[JUnitRunner])
class UrlUtilsSpec extends Specification {
  "isValidUrl" should {
    "return true for a valid url" in {
      UrlUtils.isValidUrl("http://yahoo.com") must beTrue
    }

    "return false for an invalid url" in {
      UrlUtils.isValidUrl("nohttp.com") must beFalse
    }
  }

  "getHost" should {
    "return host for a valid uri" in {
      val uri = new URI("http://google.com")
      UrlUtils.getHost(uri) must beEqualTo("google.com")
    }
  }

  "sanitizeUrl" should {
    "must strip fragments" in {
      val uri = new URI("http://google.com#hi")
      UrlUtils.sanitizeUrl(uri).toString() must beEqualTo("http://google.com")
      
    }
  }

  "createURI" should {
    "returns uri for valid uri" in {
      UrlUtils.createURI("http://google.com") must beSome
    }
    
    "returns none for invalid uri" in {
      UrlUtils.createURI("google.com") must beNone
    }
  }
}