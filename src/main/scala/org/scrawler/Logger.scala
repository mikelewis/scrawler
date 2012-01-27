package org.scrawler
import akka.actor.Actor
import akka.actor.Actor.actorOf
import akka.actor.PoisonPill

sealed trait LoggingTypes
case class Info(msg: String) extends LoggingTypes
case class Warn(msg: String) extends LoggingTypes
case class Error(msg: String) extends LoggingTypes

class LoggerActor extends Actor {
  def receive = {
    case Info(str) => message("INFO", str)
    case Warn(str) => message("WARN", str)
    case Error(str) => message("ERROR", str)
  }

  private def message(typeOfMessage: String, str: String) {
    val finalStr = "%s\t[%s] [%s] - %s".
      format(typeOfMessage, (new java.util.Date), self.channel, str)

    println(finalStr)
  }
}

object Logger {
  var loggerActor = emptyLogger

  def emptyLogger = {
    actorOf[LoggerActor].start()
  }
  
  def info(str: String) {
    sendMessage(Info(str))
  }

  def warn(str: String) {
    sendMessage(Warn(str))
  }

  def error(str: String) {
    sendMessage(Error(str))
  }

  def shutdownLogger {
    loggerActor.stop
  }
  
  private def sendMessage(msg : LoggingTypes) {
    if(loggerActor.isShutdown){
      loggerActor = emptyLogger
    }
    
    loggerActor ! msg
  }
}