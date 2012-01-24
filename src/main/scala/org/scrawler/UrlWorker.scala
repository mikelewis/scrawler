package org.scrawler
import akka.actor.Actor
import akka.actor.PoisonPill

import dispatch._

import dispatch.jsoup.JSoupHttp._

import org.jsoup.nodes.Document

/*
 * Filter out sites that don't meet header check: 
 * http://dispatch.databinder.net/Two+Handlers+Are+Better+Than+One.html
 * I.E. Content-Type, Size etc.
 * Also, some sites don't specify the size, so need a stream to automatically
 * calculate it.
 */

class UrlWorker extends Actor {
 def receive = {
   case ProcessUrl(url) =>
     // something
   	
     self.reply(processUrl(url))
 }
 
 def processUrl(url: String) : WorkType = {
   val jsoupDocument = fetchHtml(url)
   val finalDoc = jsoupDocument.fold((doc => new ParsedDocument(doc)), (failedDocument => failedDocument))
   DoneUrl(url, finalDoc)
 }

 def fetchHtml(urlStr: String) : Either[Document, FailedDocument] = {
   val h = new Http
   val doc = h(url(urlStr) </> {doc => doc})
   
   // error checking
   // otherwise return Left(doc)
   Left(doc)
 }
}