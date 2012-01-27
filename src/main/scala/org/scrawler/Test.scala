package org.scrawler

object Test extends App {
  Crawl("http://leafo.net/", CrawlConfig(maxDepth = 1))
  Crawl("http://leafo.net/", CrawlConfig(maxDepth = 1))
  
}