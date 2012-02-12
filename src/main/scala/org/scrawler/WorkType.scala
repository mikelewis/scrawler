package org.scrawler

sealed trait WorkType
case class StartCrawl(url: String) extends WorkType
case class ProcessUrl(url: String) extends WorkType
case class DoneUrl(url: String, finalDoc: FinalDocument) extends WorkType
case class StopCrawl extends WorkType