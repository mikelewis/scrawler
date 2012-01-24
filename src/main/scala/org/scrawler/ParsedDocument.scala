package org.scrawler
import org.jsoup.nodes.Document
import scala.collection.JavaConversions._


class ParsedDocument(doc: Document) extends FinalDocument {
  // TDODO I'd rather find all these links in one pass...
  val links = doc.select("a[href]").map(_.attr("abs:href"))
  val iframeSrc = doc.select("frame[src]").map(_.attr("abs:src"))
  val linkHref = doc.select("link[href]").map(_.attr("abs:href"))
  val scriptSrc = doc.select("script[src]").map(_.attr("abs:src"))
  
  def urls : List[String] = {
    (links ++ iframeSrc ++ linkHref ++ scriptSrc).toList
  }
}