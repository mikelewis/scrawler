package org.scrawler
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Actor.actorOf
import scala.collection.mutable.HashMap
import akka.actor.PoisonPill
import akka.actor.UntypedChannel


class Processor(maxDepth: Int, useSubdomain: Boolean) extends Actor {
  val currentlyProcessing = HashMap[String, ActorRef]()
  var queuedUrls = List[String]()
  var urlsProccessed = List[String]()
  var depthsProcessed = -1
  var originalRequestor: UntypedChannel = _
  
  
  def receive = {
    case StartCrawl(url) =>
      enqueueNewUrls(List(url))
      originalRequestor = self.channel
      processQueuedUrls
    case DoneUrl(startingUrl, newUrls) => 
      urlsProccessed = startingUrl :: urlsProccessed
      currentlyProcessing -= startingUrl
      enqueueNewUrls(newUrls)
      if(finishedWithCurrentDepth){
        depthsProcessed += 1
        if(!isFinished){
          processQueuedUrls
        } else {
          originalRequestor ! urlsProccessed
        }
      }     
  }
  
   def isFinished: Boolean = {
	  depthsProcessed == maxDepth
   }
   
   def finishedWithCurrentDepth: Boolean = {
     currentlyProcessing.size == 0
   }
  
  def processQueuedUrls {
    val tempList: List[String] = queuedUrls
    queuedUrls = emptyList
    tempList.foreach{ url => 
      	if(!currentlyProcessing.contains(url)){
      		val actor = actorOf[UrlWorker]
      		currentlyProcessing += (url -> actor.start())
      		actor ! ProcessUrl(url)
      	}
    }
  }
  
  def enqueueNewUrls(urls: List[String])  {
    queuedUrls = urls ::: queuedUrls
  }
  
  def emptyList = {
    List[String]()
  }
  
}