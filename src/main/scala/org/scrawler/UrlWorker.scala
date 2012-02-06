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
  val hooks = crawlConfig.hooks

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
      case e: Exception => new Right(SystemError(url, e))
    }
    val finalDoc = jsoupDocument.fold((doc => doc), (failedDocument => failedDocument))
    DoneUrl(url, finalDoc)
  }

  def fetchHtml(urlStr: String): Either[ParsedDocument, FailedDocument] = {
    try {
      val response = client.prepareGet(urlStr).execute(generateHttpHandler).get()
      if (!response.hasResponseHeaders())
        return Right(AbortedDocumentDuringStatus(response.getUri.toString))
      if (!response.hasResponseBody)
        return Right(AbortedDocumentDuringHeaders(response.getUri.toString))
        
      val doc = Jsoup.parse(response.getResponseBodyAsStream(), null, response.getUri.toString())
      Left(new ParsedDocument(response, doc))
    } catch {
      case x => {
        x.getCause() match {
          case e: java.net.ConnectException => Right(ConnectionError(urlStr, e))
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
        val newBuilder = builder.accumulate(bodyPart)
        if (hooks.canContinueFromBodyPartReceived(newBuilder.build, bodyPart))
          STATE.CONTINUE
        else
          STATE.ABORT
      }

      def onStatusReceived(responseStatus: HttpResponseStatus) = {
        val newBuilder = builder.accumulate(responseStatus)
        if (hooks.canContinueFromStatusCode(newBuilder.build, responseStatus.getStatusCode()))
          STATE.CONTINUE
        else
          STATE.ABORT
      }

      def onHeadersReceived(headers: HttpResponseHeaders) = {
        val newBuilder = builder.accumulate(headers)
        val resp = newBuilder.build
        if (hooks.canContinueFromHeaders(resp, GeneralUtils.getHeadersFromResponse(resp)))
          STATE.CONTINUE
        else
          STATE.ABORT
      }

      def onCompleted() = {
        builder.build()
      }
    }
  }
}