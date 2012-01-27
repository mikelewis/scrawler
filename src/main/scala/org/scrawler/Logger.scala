package org.scrawler
import akka.actor.Actor
import akka.actor.Actor.actorOf
import akka.actor.PoisonPill

sealed trait LoggingTypes
case class Info(sender: Any, msg: String) extends LoggingTypes
case class Warn(sender: Any, msg: String) extends LoggingTypes
case class Error(sender: Any, msg: String) extends LoggingTypes

class LoggerActor extends Actor {
  def receive = {
    case Info(from, str) => message("INFO", from, str)
    case Warn(from, str) => message("WARN", from, str)
    case Error(from, str) => message("ERROR", from, str)
  }

  private def message(typeOfMessage: String, from : Any, str: String) {
    val finalStr = "%s\t[%s] [%s] - %s".
      format(typeOfMessage, (new java.util.Date), from, str)

    println(finalStr)
  }
}

object Logger {
  var loggerActor = emptyLogger

  def emptyLogger = {
    actorOf[LoggerActor].start()
  }

  def info(from : Any, str: String) {
    sendMessage(Info(from, str))
  }

  def warn(from : Any, str: String) {
    sendMessage(Warn(from, str))
  }

  def error(from : Any, str: String) {
    sendMessage(Error(from, str))
  }

  def shutdownLogger {
    loggerActor.stop
  }

  private def sendMessage(msg: LoggingTypes) {
    if (loggerActor.isShutdown) {
      loggerActor = emptyLogger
    }

    loggerActor ! msg
  }
}