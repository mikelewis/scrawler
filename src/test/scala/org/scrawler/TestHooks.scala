package org.scrawler
import com.ning.http.client.Response
import com.ning.http.client.HttpResponseBodyPart

trait TestHooks {
  object TestHeaderHookFail extends Hooks {
    override def canContinueFromHeaders(response: Response, headers: Map[String, String]) = {
      false
    }
  }

  object TestHeaderHookPass extends Hooks {
    override def canContinueFromHeaders(response: Response, headers: Map[String, String]) = {
      true
    }
  }

  object TestStatusHookFail extends Hooks {
    override def canContinueFromStatusCode(response: Response, status: Integer) = {
      false
    }
  }

  object TestStatusHookPass extends Hooks {
    override def canContinueFromStatusCode(response: Response, status: Integer) = {
      true
    }
  }

  object TestBodyPartHookFail extends Hooks {
    override def canContinueFromBodyPartReceived(response: Response, part: HttpResponseBodyPart) = {
      response.getResponseBody().length < 1193
    }
  }

  object TestBodyPartHookPass extends Hooks {
    override def canContinueFromBodyPartReceived(response: Response, part: HttpResponseBodyPart) = {
      true
    }
  }
}