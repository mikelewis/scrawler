package org.scrawler
import com.ning.http.util.AsyncHttpProviderUtils
import java.net.URI

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
    new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
      uri.getQuery(), null)
  }

  def createURI(url: String): Option[URI] = {
    try {
      Some(AsyncHttpProviderUtils.createUri(url))
    } catch {
      case e => None
    }
  }
}