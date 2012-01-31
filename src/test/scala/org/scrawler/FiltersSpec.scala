package org.scrawler
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.{ Before, After, Step, Around }
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class FiltersSpec extends Specification {

  trait before extends Scope {
    class SampleClassWithFilter extends Filters {
      var crawlConfig = CrawlConfig()
    }

    val sample = new SampleClassWithFilter
  }

  "Filters" should {
    "valid hosts should return true if hosts array is empty" in new before {
      sample.validHost("something.com") must beTrue
    }

    "valid hosts should return true if host exist in regex array" in new before {
      sample.crawlConfig = CrawlConfig(hosts = Seq("""awesome.com""".r))
      sample.validHost("awesome.com") must beTrue
    }

    "invalid url should return false if url seq is empty" in new before {
      sample.invalidUrl("http://somethingawesome.com/hey") must beFalse
    }

    "invalid url should return true if matches" in new before {
      sample.crawlConfig = CrawlConfig(ignoreLinks = Seq("""moonscript""".r))
      sample.invalidUrl("http://somethingawesome.com/moonscript") must beFalse
    }
  }

}

 