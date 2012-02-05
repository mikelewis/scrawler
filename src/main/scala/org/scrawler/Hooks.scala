package org.scrawler

import com.ning.http.client.HttpResponseHeaders
import com.ning.http.client.HttpResponseStatus
import com.ning.http.client.Response
import com.ning.http.client.HttpResponseBodyPart

class Hooks {
  def canVisitUrl(url: String): Boolean = {
    true
  }

  def canContinueFromHeaders(response: Response, headers: Map[String, String]) = {
    true
  }

  def canContinueFromStatusCode(response: Response, status: Integer) = {
    true
  }

  def canContinueFromBodyPartReceived(response: Response, bodyPart: HttpResponseBodyPart) = {
    true
  }
}