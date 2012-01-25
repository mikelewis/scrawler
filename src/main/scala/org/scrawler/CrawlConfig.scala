package org.scrawler
import scala.util.matching.Regex
import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.AsyncHttpClientConfig.Builder

case class CrawlConfig(maxDepth : Int = -1, hosts : Traversable[Regex] = Seq(), 
    ignoreLinks : Traversable[Regex] = Seq(), httpClientConfig : AsyncHttpClientConfig)