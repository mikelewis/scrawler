package org.scrawler
import org.jsoup.nodes.Document
import com.ning.http.client.Response

import scala.collection.JavaConversions._

class ParsedDocument(val response: Response, val doc: Document) extends FinalDocument {
  val urls = doc.select("a[href],frame[src],link[href],script[src]").foldLeft(List.empty[String]) { (list, e) =>
    val url = e.tagName() match {
      case "a" | "link" => e.attr("abs:href")
      case "frame" | "script" => e.attr("abs:src")
    }
    url :: list
  }

  val statusCode = response.getStatusCode
  val headers = GeneralUtils.getHeadersFromResponse(response)
  val body = response.getResponseBody
}