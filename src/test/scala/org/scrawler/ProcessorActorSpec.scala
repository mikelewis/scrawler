package org.scrawler
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfterEach

@RunWith(classOf[JUnitRunner])
class ProcessorActorSpec extends RunTestServer with BeforeAndAfterEach {
  var actor = testActualProcessor()

  override def beforeEach() {
    actor = testActualProcessor()
  }

  def setActor(crawlConfig: CrawlConfig) {
    actor = testActualProcessor(crawlConfig)
  }

  describe("integration tests") {
    it("should not get into a cycle") {
      setActor(CrawlConfig(maxDepth = 2))
      actor.start()
      val result = actor ? StartCrawl(getUrl("cycle.html"))
      result.get.asInstanceOf[List[String]].toSet should be(relativeToAbsolute("cycle.html", "cycle1.html").toSet)
    }

    it("should default maxDepth is infinite") {
      actor.start()
      val result = actor ? StartCrawl(getUrl("cycle.html"))
      result.get.asInstanceOf[List[String]].toSet should be(relativeToAbsolute("cycle.html", "cycle1.html").toSet)
    }

    it("not process invalid links") {
      actor.start()
      val result = actor ? StartCrawl(getUrl("invalid_links.html"))
      result.get.asInstanceOf[List[String]].toSet should be(List("http://somethingthatdoesntexist-butisval1d.com", getUrl("invalid_links.html")).toSet)
    }

    it("should maxDepth rule handle 0") {
      setActor(CrawlConfig(maxDepth = 0))
      actor.start()
      val result = actor ? StartCrawl(getUrl("invalid_links.html"))
      result.get.asInstanceOf[List[String]].toSet should be(relativeToAbsolute("invalid_links.html").toSet)
    }

    it("should maxDepth rule handle 1") {
      setActor(CrawlConfig(maxDepth = 1))
      actor.start()
      val result = actor ? StartCrawl(getUrl("1.html"))
      result.get.asInstanceOf[List[String]].toSet should be(relativeToAbsolute("2.html", "3.html", "1.html").toSet)
    }

    it("should maxDepth rule handle 2") {
      setActor(CrawlConfig(maxDepth = 2))
      actor.start()
      val result = actor ? StartCrawl(getUrl("1.html"))
      result.get.asInstanceOf[List[String]].toSet should be(relativeToAbsolute("4.html", "5.html", "6.html", "7.html", "2.html", "3.html", "1.html").toSet)
    }
  }
}