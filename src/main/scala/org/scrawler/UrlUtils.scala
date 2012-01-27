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
  
  def getHost(uri : URI) = {
    Option(uri.getHost()).getOrElse(uri.getAuthority())
  }
  
  def createURI(url : String) : Option[URI] = {
   try {
    Some(AsyncHttpProviderUtils.createUri(url))
   } catch {
     case e => None
   }
  }
}