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
    case Info(str) => message("info", str)
    case Warn(str) => message("warn", str)
    case Error(str) => message("error", str)
  }

  private def message(typeOfMessage: String, str: String) {
    val finalStr = "[%s] [%s] [%s] - %s".
      format((new java.util.Date), typeOfMessage.capitalize, self.channel, str)

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