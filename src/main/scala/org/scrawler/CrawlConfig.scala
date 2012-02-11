package org.scrawler
import scala.util.matching.Regex
import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.AsyncHttpClientConfig.Builder
import akka.actor.Actor.actorOf
import akka.actor.ActorRef

case class CrawlConfig(
  maxDepth: Int = scala.Int.MaxValue,
  maxUrls: Int = scala.Int.MaxValue,
  hosts: Traversable[Regex] = Seq(),
  numberOfUrlWorkers: Int = 10,
  httpClientConfig: AsyncHttpClientConfig = GeneralUtils.defaultAsyncHttpConfig,
  hooks: Hooks = DefaultHooks,
  callbacks: ActorRef = actorOf[DefaultCallbacks].start()) {}