package org.scrawler
import akka.dispatch.Dispatchers

object Dispatcher {
  val dispatcher = Dispatchers.newExecutorBasedEventDrivenWorkStealingDispatcher("crawler-dispatcher").build
}