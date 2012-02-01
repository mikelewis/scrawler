package org.scrawler
import akka.actor.Actor

sealed trait CallbackMessage
case class ProcessedUrl(url: String) extends CallbackMessage
case class ProcessedDocument(doc: FinalDocument) extends CallbackMessage
case class ProcessedSuccessfulDocument(doc: ParsedDocument) extends CallbackMessage
case class ProcessedFailedDocument(doc: FailedDocument) extends CallbackMessage

class Callbacks extends Actor {

  def receive = {
    case ProcessedUrl(url) => proccessedUrl(url)
    case ProcessedDocument(doc: FinalDocument) => processedDocument(doc)
    case ProcessedSuccessfulDocument(doc: ParsedDocument) => processedSuccessfulDocument(doc)
    case ProcessedFailedDocument(doc: ParsedDocument) => processedFailedDocument(doc)
  }

  def processedDocument(doc: FinalDocument) {

  }

  def processedSuccessfulDocument(doc: ParsedDocument) {

  }

  def processedFailedDocument(doc: FailedDocument) {

  }

  def proccessedUrl(url: String) {

  }
}