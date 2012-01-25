package org.scrawler
import com.ning.http.util.AsyncHttpProviderUtils

object UrlUtils {
  def isValidUrl(url: String): Boolean = {
   try {
    AsyncHttpProviderUtils.createUri(url)
    true
   } catch {
     case e => false
   }
  }
}