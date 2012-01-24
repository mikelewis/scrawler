package org.scrawler
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Actor.actorOf
import scala.collection.mutable.HashMap
import akka.actor.PoisonPill
import akka.actor.UntypedChannel



class Processor(maxDepth: Int, useSubdomain: Boolean) extends Actor {
  // Master list of urls currently being processed.
  val currentlyProcessing = HashMap[String, ActorRef]()
  // Urls that are queued for the next depth
  val queuedUrls = emptyQueue
  // Keep track of urls we've processed.
  val urlsProcessed = scala.collection.mutable.Set[String]()
  var depthsProcessed = -1 // Keep track of the depth as we are do a BFS(Breadth First Search)
  var originalRequestor: UntypedChannel = _ // Know where to send results back to
  
  
  def receive = {
    case StartCrawl(url) =>
      enqueueNewUrls(List(url))
      originalRequestor = self.channel
      processQueuedUrls
      
    case DoneUrl(startingUrl, finalDocument) => 
      urlsProcessed += startingUrl
      currentlyProcessing -= startingUrl
      handleDoneUrl(finalDocument)
  }
  
  def handleDoneUrl(finalDocument: FinalDocument) {
    finalDocument match {
      case parsedDoc: ParsedDocument => handleParsedDocument(parsedDoc)
      case failedDoc: FailedDocument => handleFailedDocument(failedDoc)
    }
    
    if(finishedWithCurrentDepth){
      depthsProcessed += 1
      println("Depths processed" + depthsProcessed)
      if(!isFinished){
        processQueuedUrls
       } else {
         originalRequestor ! urlsProcessed.toList
       }
     }
  }
  
  def handleParsedDocument(parsedDocument: ParsedDocument){
    println("Got " + parsedDocument.urls.size + " urls to process" )
    println("Number to go " + currentlyProcessing.size)
    enqueueNewUrls(parsedDocument.urls)
  }
  
  def handleFailedDocument(failedDocument: FailedDocument){
    // pass to some callback with failures?
  }
  
   def isFinished: Boolean = {
	  queuedUrls.size == 0 || depthsProcessed == maxDepth
   }
   
   def finishedWithCurrentDepth: Boolean = {
     currentlyProcessing.size == 0
   }
  
  def processQueuedUrls {
    queuedUrls.dequeueAll( e=> true ).foreach{ url => 
      	val actor = actorOf[UrlWorker]
      	currentlyProcessing += (url -> actor.start())
      	actor ! ProcessUrl(url)
    }
    queuedUrls.clear
  }
  
  def enqueueNewUrls(urls: List[String])  {
    urls.foreach{ url =>
    	if(!queuedUrls.contains(url) && visit(url)){
    	  queuedUrls += url
    	}
    }
  }
  
  def visit(url: String) : Boolean = {
    // also make sure it's on the same host etc etc.
    !urlsProcessed(url)
  }
  
  def emptyQueue = {
    scala.collection.mutable.Queue[String]()
  }
  
}