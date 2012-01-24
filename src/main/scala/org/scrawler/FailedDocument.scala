package org.scrawler

sealed trait FailedDocument extends FinalDocument
case class DocumentTooLong extends FailedDocument
case class InvalidDocumentFormat extends FailedDocument
case class SystemError(e: Exception) extends FailedDocument