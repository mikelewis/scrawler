package org.scrawler
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfterEach
import org.scalatest.BeforeAndAfter

@RunWith(classOf[JUnitRunner])
class FiltersSpec extends MasterSuite with BeforeAndAfter {
  class SampleClassWithFilter extends Filters {
    var crawlConfig = CrawlConfig()
  }
  var sample = new SampleClassWithFilter
  
  before {
    sample = new SampleClassWithFilter
  }

  describe("validHosts") {
    it("should return true if hosts array is empty") {
      sample.validHost("something.com") should be(true)
    }

    it("should return true if host exist in regex array") {
      sample.crawlConfig = CrawlConfig(hosts = Seq("""awesome.com""".r))
      sample.validHost("awesome.com") should be(true)
    }
  }
}