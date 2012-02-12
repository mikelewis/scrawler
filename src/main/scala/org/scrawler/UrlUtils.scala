package org.scrawler
import com.ning.http.util.AsyncHttpProviderUtils
import java.net.URI
import java.net.URLDecoder
import scala.collection.immutable.TreeMap
import java.net.URLEncoder
import java.io.UnsupportedEncodingException

object UrlUtils {
  def isValidUrl(url: String): Boolean = {
    try {
      AsyncHttpProviderUtils.createUri(url)
      true
    } catch {
      case e => false
    }
  }

  // assumes url is correct
  def getHost(uri: URI) = {
    Option(uri.getHost()).getOrElse(uri.getAuthority())
  }

  // assumed to be a valid uri
  def sanitizeUrl(uri: URI) = {
    // strip fragments
    val newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
      uri.getQuery(), null)

    val port = newUri.getPort
    // Option to safe guard against null (thanks Java :/ )
    var queryString = generateCanonicalQueryString(Option(newUri.getQuery()))

    newUri.getScheme().toLowerCase +
      "://" + newUri.getHost.toLowerCase +
      (if (port != -1 && port != 80) ":" + port else "") +
      newUri.getPath +
      (if (queryString == "") "" else "?" + queryString)
  }

  def createURI(url: String): Option[URI] = {
    try {
      Some(AsyncHttpProviderUtils.createUri(url))
    } catch {
      case e => None
    }
  }

  def generateCanonicalQueryString(path: Option[String]): String = {
    path.map { p =>
      val splitQuery = p.split("&")
      val resultStringBuilder = splitQuery.foldLeft(TreeMap[String, String]()) { (map, pair) =>
        // decode tokens
        val tokens = pair.split("=", 2).map { v =>
          try {
            URLDecoder.decode(v, "UTF-8")
          } catch {
            case x: UnsupportedEncodingException => v
          }
        }

        tokens.length match {
          case 1 if pair(0) == '=' => map + ("" -> tokens(0))
          case 1 => map + (tokens(0) -> "")
          case 2 => map + (tokens(0) -> tokens(1))

          // filter out useless params like session or utm_*
        }
      }.foldLeft(new StringBuilder()) {
        case (queryString, (key, value)) =>
          queryString.appendAll(percentEncodeRfc3986(key)).appendAll("=").appendAll(percentEncodeRfc3986(value)).appendAll("&")
      }

      finalizeStringBuilder(splitQuery, resultStringBuilder).mkString
    }.getOrElse("")
  }

  def finalizeStringBuilder(splitQuery: Array[String], str: StringBuilder) = {
    var copyStr = str

    copyStr = if (copyStr.endsWith("&"))
      copyStr.dropRight(1)
    else
      copyStr
    if (splitQuery.size == 1 && copyStr.endsWith("="))
      copyStr.dropRight(1)
    else
      copyStr
  }

  // Thanks http://stackoverflow.com/questions/2993649/how-to-normalize-a-url-in-java
  def percentEncodeRfc3986(str: String): String = {
    try {
      URLEncoder.encode(str, "UTF-8")
    } catch {
      case x: UnsupportedEncodingException => str
    }
  }
}