package org.scrawler
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Actor.actorOf
import scala.collection.mutable.HashMap
import akka.actor.PoisonPill
import akka.actor.UntypedChannel
import akka.routing.Routing.Broadcast
import akka.routing.CyclicIterator
import akka.routing.Routing
import akka.config.Supervision.OneForOneStrategy
import akka.config.Supervision.Permanent
import akka.dispatch.Dispatchers
import java.net.URI

class Processor(val crawlConfig: CrawlConfig) extends Actor with Filters {
  self.faultHandler = OneForOneStrategy(List(classOf[Throwable]), 5, 5000)

  val maxDepth = crawlConfig.maxDepth
  // Master list of urls currently being processed.
  val currentlyProcessing = scala.collection.mutable.Set[String]()
  // Urls that are queued for the next depth
  val queuedUrls = emptyQueue
  // Keep track of urls we've processed.
  val urlsProcessed = scala.collection.mutable.Set[String]()
  var depthsProcessed = -1 // Keep track of the depth as we are do a BFS(Breadth First Search)
  var originalRequestor: UntypedChannel = _ // Know where to send results back to

  // Arbitrary number for now
  val urlWorkers = Vector.fill(crawlConfig.numberOfUrlWorkers)(actorOf(new UrlWorker(crawlConfig)))
  val workerRouter = Routing.loadBalancerActor(CyclicIterator(urlWorkers)).start()

  override def preStart = urlWorkers foreach { self.startLink(_) }
  override def postStop() {
    urlWorkers.foreach(self.unlink(_))

    workerRouter ! Broadcast(PoisonPill)

    workerRouter ! PoisonPill
  }

  def receive = {
    case StartCrawl(url) =>
      enqueueNewUrls(List(url))
      originalRequestor = self.channel
      processQueuedUrlsOrFinish

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

    if (finishedWithCurrentDepth) {
      depthsProcessed += 1
      println("Depths processed" + depthsProcessed)
      processQueuedUrlsOrFinish
    }
  }

  def processQueuedUrlsOrFinish {
    if (!isFinished) {
      processQueuedUrls
    } else {
      originalRequestor ! urlsProcessed.toList
    }
  }

  def handleParsedDocument(parsedDocument: ParsedDocument) {
    println("Got " + parsedDocument.urls.size + " urls to process")
    println("Number to go " + currentlyProcessing.size)
    enqueueNewUrls(parsedDocument.urls)
  }

  def handleFailedDocument(failedDocument: FailedDocument) {
    // pass to some callback with failures?
    failedDocument match {
      case SystemError(e) => println("Exception!!" + e); e.printStackTrace()
      case x => println("Something else" + x)
    }
  }

  def isFinished: Boolean = {
    queuedUrls.size == 0 || depthsProcessed == maxDepth
  }

  def finishedWithCurrentDepth: Boolean = {
    currentlyProcessing.size == 0
  }

  def processQueuedUrls {
    queuedUrls.dequeueAll(e => true).foreach { url =>
      currentlyProcessing += url

      workerRouter ! ProcessUrl(url)
    }
    queuedUrls.clear
  }

  def enqueueNewUrls(urls: List[String]) {
    urls.foreach { url =>
      if (!queuedUrls.contains(url) && visit(url)) {
        queuedUrls += url
      }
    }
  }

  def visit(url: String): Boolean = {
    val urlObj: URI = UrlUtils.createURI(url).toRight(None).
      fold(_ => return false, x => x)

    // also make sure it's on the same host etc etc.
    !urlsProcessed(url) &&
      validHost(urlObj.getHost) &&
      !invalidUrl(url)
  }

  def emptyQueue = {
    scala.collection.mutable.Queue[String]()
  }

}