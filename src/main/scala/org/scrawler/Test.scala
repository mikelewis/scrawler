package org.scrawler

object Test extends App {
  Crawl("http://leafo.net/", CrawlConfig(maxDepth = 1))
  Crawl.host("leafo.net", false, CrawlConfig(maxDepth = 1, ignoreLinks = Seq("""moonscript""".r)))
}