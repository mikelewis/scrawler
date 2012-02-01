package org.scrawler

import com.ning.http.client.HttpResponseHeaders
import com.ning.http.client.HttpResponseStatus

class Hooks {
  def canVisitUrl(url: String): Boolean = {
    true
  }

  def canContinueFromHeaders(headers: HttpResponseHeaders) = {
    true
  }

  def canContinueFromStatusCode(url: String, status: Integer) = {
    true
  }
}