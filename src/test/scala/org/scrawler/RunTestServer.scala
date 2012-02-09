package org.scrawler
import org.scalatest.BeforeAndAfterAll
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.DefaultHandler
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.ResourceHandler

class RunTestServer extends MasterSuite with BeforeAndAfterAll {
  var actualPort = 0
  val actualBaseUrl = "http://localhost"
  override def beforeAll(configMap: Map[String, Any]) {
    val server = new Server(0)
    val resource_handler = new ResourceHandler
    resource_handler.setDirectoriesListed(true)
    resource_handler.setResourceBase("sample_files")

    val handlers = new HandlerList
    handlers.setHandlers(Array[Handler](resource_handler, new DefaultHandler()))
    server.setHandler(handlers)

    server.start
    actualPort = server.getConnectors()(0).getLocalPort()
  }
  
  def getUrl(path: String) = {
    actualBaseUrl + ":" + actualPort + "/" + path
  }
  
  def relativeToAbsolute(paths: List[String]) = {
    paths.map(path => getUrl(path))
  }
  
  def relativeToAbsolute(str: String*): List[String] = {
    relativeToAbsolute(str.toList)
  }
}