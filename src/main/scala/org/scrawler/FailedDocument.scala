package org.scrawler

sealed trait FailedDocument extends FinalDocument
case class DocumentTooLong(url: String) extends FailedDocument
case class InvalidDocumentFormat(url: String) extends FailedDocument
case class SystemError(url: String, e: Exception) extends FailedDocument
case class ConnectionError(url: String, e: java.net.ConnectException) extends FailedDocument