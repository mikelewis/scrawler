package org.scrawler

sealed trait WorkType
case class StartCrawl(url: String) extends WorkType
case class ProcessUrl(url: String) extends WorkType
case class DoneUrl(url: String, urls: List[String]) extends WorkType