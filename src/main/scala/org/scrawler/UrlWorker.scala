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



import akka.actor.Actor
import akka.actor.PoisonPill
import akka.event.EventHandler


import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import collection.JavaConversions._


class UrlWorker extends Actor {
  val client = new AsyncHttpClient(generateAsyncBuilder)
      
 def receive = {
   case ProcessUrl(url) =>	
     self.reply(processUrl(url))
 }
 
 def processUrl(url: String) : WorkType = {
   val jsoupDocument = try {
	  fetchHtml(url)
   } catch {
     case e : Exception => new Right(SystemError(e))
   }
   val finalDoc = jsoupDocument.fold((doc => new ParsedDocument(doc)), (failedDocument => failedDocument))
   DoneUrl(url, finalDoc)
 }

 def fetchHtml(urlStr: String) : Either[Document, FailedDocument] = {
    val response = client.prepareGet(urlStr).execute(generateHttpHandler).get()
    
    Left(Jsoup.parse(response.getResponseBodyAsStream(), null, response.getUri.toString()))
 }
 
 def generateAsyncBuilder = {
    val builder = new AsyncHttpClientConfig.Builder()
    builder.setCompressionEnabled(true)
        .setAllowPoolingConnection(true)
        .setMaximumNumberOfRedirects(5)
        .setRequestTimeoutInMs(30000)
        .setFollowRedirects(true)
        .build()
 }
 
 def generateHttpHandler = {
   new AsyncHandler[Response]() {
          val builder =
            new Response.ResponseBuilder()

          def onThrowable(t: Throwable) {
            EventHandler.error(this, t.getMessage)
          }

          def onBodyPartReceived(bodyPart: HttpResponseBodyPart) = {
            builder.accumulate(bodyPart)
            STATE.CONTINUE
          }

          def onStatusReceived(responseStatus: HttpResponseStatus) = {
            EventHandler.info(this, "Status: %s".format(responseStatus.getStatusCode()))
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