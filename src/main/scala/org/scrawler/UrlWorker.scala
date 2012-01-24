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

/*
 * Filter out sites that don't meet header check: 
 * http://dispatch.databinder.net/Two+Handlers+Are+Better+Than+One.html
 * I.E. Content-Type, Size etc.
 * Also, some sites don't specify the size, so need a stream to automatically
 * calculate it.
 */

class UrlWorker extends Actor {
  val client = new AsyncHttpClient(generateAsyncBuilder)
      
 def receive = {
   case ProcessUrl(url) =>	
     self.reply(processUrl(url))
 }
 
 def processUrl(url: String) : WorkType = {
   val jsoupDocument = fetchHtml(url)
   val finalDoc = jsoupDocument.fold((doc => new ParsedDocument(doc)), (failedDocument => failedDocument))
   DoneUrl(url, finalDoc)
 }

 def fetchHtml(urlStr: String) : Either[Document, FailedDocument] = {
   
    val httpHandler = new AsyncHandler[Response]() {
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
    

    val response = client.prepareGet(urlStr).execute(httpHandler).get()
    
    Left(Jsoup.parse(response.getResponseBodyAsStream(), null, urlStr))
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
}