package org.scrawler
import scala.util.matching.Regex

trait Filters {
  def crawlConfig: CrawlConfig

  def validHost(host: String) = {
    crawlConfig.hosts.isEmpty || GeneralUtils.genericRegexMatch(crawlConfig.hosts, host)
  }

  // TODOD DO
  def validScheme(scheme: String) = {
    true
  }

  // TODO DO
  def validPort(port: Integer) = {
    true
  }

}