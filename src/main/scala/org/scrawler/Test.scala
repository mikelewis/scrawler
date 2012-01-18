package org.scrawler

object Test extends App {
  Crawl("http://google.com", maxDepth = 3)
}