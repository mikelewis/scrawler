package org.scrawler
import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.AsyncHttpClientConfig.Builder
import scala.util.matching.Regex
import com.ning.http.client.Response
import collection.JavaConversions._


object GeneralUtils {
  def defaultAsyncHttpConfig = {
    val builder = new AsyncHttpClientConfig.Builder()
    builder.setCompressionEnabled(true)
      .setAllowPoolingConnection(true)
      .setMaximumNumberOfRedirects(5)
      .setRequestTimeoutInMs(30000)
      .setFollowRedirects(true)
      .build()
  }

  def getHeadersFromResponse(response: Response): Map[String, String] = {
    val headers = response.getHeaders()
    headers.keySet.foldLeft(Map.empty[String, String]) { (acum, header) =>
      acum + (header -> headers.getJoinedValue(header, ","))
    }
  }

  def genericRegexMatch(regexes: Traversable[Regex], str: String) = {
    regexes.exists { regex =>
      regex.findFirstIn(str).isDefined
    }
  }
}