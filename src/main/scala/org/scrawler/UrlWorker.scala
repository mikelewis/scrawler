package org.scrawler

import com.ning.http.client.AsyncHandler.STATE
import com.ning.http.client.AsyncHandler
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.HttpResponseBodyPart
import com.ning.http.client.HttpResponseHeaders
import com.ning.http.client.HttpResponseStatus
import com.ning.http.client.Response
import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.AsyncHttpClientConfig.Builder

import java.net.ConnectException

import akka.actor.Actor
import akka.actor.PoisonPill

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import collection.JavaConversions._

class UrlWorker(crawlConfig: CrawlConfig) extends Actor {
  val client = new AsyncHttpClient(crawlConfig.httpClientConfig)

  override def postStop {
    client.close
  }

  def receive = {
    case ProcessUrl(url) =>
      self.reply(processUrl(url))
  }

  def processUrl(url: String): WorkType = {
    val jsoupDocument = try {
      fetchHtml(url)
    } catch {
      case e: Exception => new Right(SystemError(url ,e))
    }
    val finalDoc = jsoupDocument.fold((doc => doc), (failedDocument => failedDocument))
    DoneUrl(url, finalDoc)
  }

  // TODO - Based on response, return Right(FailedDocument)
  def fetchHtml(urlStr: String): Either[ParsedDocument, FailedDocument] = {
    try {
      val response = client.prepareGet(urlStr).execute(generateHttpHandler).get()
      val doc = Jsoup.parse(response.getResponseBodyAsStream(), null, response.getUri.toString())
      Left(new ParsedDocument(response, doc))
    } catch {
      case x => {
    	  x.getCause() match {
    	    case e : java.net.ConnectException => Right(ConnectionError(urlStr, e))
    	  }
      }
    }
  }

  def generateHttpHandler = {
    new AsyncHandler[Response]() {
      val builder =
        new Response.ResponseBuilder()

      def onThrowable(t: Throwable) {
        Logger.error(this, t.getMessage)
      }

      def onBodyPartReceived(bodyPart: HttpResponseBodyPart) = {
        builder.accumulate(bodyPart)
        STATE.CONTINUE
      }

      def onStatusReceived(responseStatus: HttpResponseStatus) = {
        Logger.info(this, "Status: %s".format(responseStatus.getStatusCode()))
        builder.accumulate(responseStatus)
        STATE.CONTINUE
      }
      def onHeadersReceived(headers: HttpResponseHeaders) = {
        builder.accumulate(headers)
        STATE.CONTINUE
      }

      def onCompleted() = {
        builder.build()
      }
    }
  }
}