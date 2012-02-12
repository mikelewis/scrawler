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
import com.ning.http.client.AsyncHttpClient

class Processor(val crawlConfig: CrawlConfig) extends Actor with Filters {
  self.faultHandler = OneForOneStrategy(List(classOf[Throwable]), 5, 5000)

  val hooks = crawlConfig.hooks
  val callbacks = crawlConfig.callbacks
  val maxDepth = crawlConfig.maxDepth

  var gracefulShutdown = false
  // Master list of urls currently being processed.
  val currentlyProcessing = scala.collection.mutable.Set[String]()
  // Urls that are queued for the next depth
  val queuedUrls = emptyQueue
  // Keep track of urls we've processed.
  val urlsProcessed = scala.collection.mutable.Set[String]()
  var depthsProcessed = -1 // Keep track of the depth as we are do a BFS(Breadth First Search)
  var originalRequestor: UntypedChannel = _ // Know where to send results back to

  val httpClient = new AsyncHttpClient(crawlConfig.httpClientConfig)

  val urlWorkers = Vector.fill(crawlConfig.numberOfUrlWorkers)(actorOf(new UrlWorker(httpClient, crawlConfig)))
  val workerRouter = Routing.loadBalancerActor(CyclicIterator(urlWorkers)).start()

  override def preStart = urlWorkers foreach { self.startLink(_) }
  override def postStop() {
    urlWorkers.foreach(self.unlink(_))

    workerRouter ! Broadcast(PoisonPill)

    workerRouter ! PoisonPill

    httpClient.close
  }

  def receive = {
    case StartCrawl(url) =>
      enqueueNewUrls(List(url))
      originalRequestor = self.channel
      processQueuedUrlsOrFinish

    case DoneUrl(startingUrl, finalDocument) =>
      currentlyProcessing -= startingUrl
      urlsProcessed += startingUrl
      callbacks ! ProcessedUrl(startingUrl)
      handleDoneUrl(finalDocument)

    case StopCrawl => originalRequestor = self.channel; startGracefulShutdown
    case _ =>
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
    if (!isFinished && !gracefulShutdown) {
      processQueuedUrls
    } else {
      finishProcessing
    }
  }

  def startGracefulShutdown {
    gracefulShutdown = true
  }

  def finishProcessing {
    originalRequestor ! urlsProcessed.toList
  }

  def handleParsedDocument(parsedDocument: ParsedDocument) {
    println("Got " + parsedDocument.urls.size + " urls to process")
    println("Number to go " + currentlyProcessing.size)
    if (!gracefulShutdown)
      enqueueNewUrls(parsedDocument.urls)
  }

  def handleFailedDocument(failedDocument: FailedDocument) {
    // pass to some callback with failures?
    failedDocument match {
      case SystemError(url, e) => println("Exception for " + url + "!!" + e); e.printStackTrace()
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
      validateAndSanitizeUrl(url).map { url =>
        if (!queuedUrls.contains(url) && visit(url)) {
          queuedUrls += url
        }
      }
    }
  }

  def validateAndSanitizeUrl(url: String): Option[String] = {
    UrlUtils.createURI(url).map { uri =>
      UrlUtils.sanitizeUrl(uri).toString
    }
  }

  // Assumed to be a valid url
  def visit(url: String): Boolean = {
    val urlObj: URI = UrlUtils.createURI(url).get

    // also make sure it's on the same host etc etc.

    !urlsProcessed(url) &&
      validHost(UrlUtils.getHost(urlObj)) &&
      hooks.canVisitUrl(url)
  }

  def emptyQueue = {
    scala.collection.mutable.Queue[String]()
  }

}