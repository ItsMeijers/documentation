package com.itsmeijers.actors

import akka.actor._
import java.io.File
import DocumentRetriever._
import net.ruippeixotog.scalascraper.browser.Browser
import scala.util.{Try, Success, Failure}

class DocumentRetriever extends Actor with ActorLogging {

   val browser = new Browser

   def receive = {
      case RetrieveDocs(location) =>
      // scala = location + "/scala"
      // deprecetedFile = location + "/deprecated-list.html"
      // index = location + "/index"
         Try(new File(getClass.getResource(location).toURI)) match {
           case Failure(e) =>
             println("****************failure!")
             sender() ! e.getMessage()
           case Success(dir) => {
             val files = getFileTree(dir)
             val htmlFiles = files.filter(f => """.*\.html$""".r.findFirstIn(f.getName).isDefined)

             sender() ! ParserScheduler.AmountOfDocuments(htmlFiles.length)

             htmlFiles.foreach { f =>
                val document = browser.parseFile(f)
                sender() ! ParserScheduler.RouteParseDocument(document)
             }
           }
         }
   }

   def getFileTree(f: File): Stream[File] = f #:: (
      if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
      else Stream.empty
   )

}

object DocumentRetriever {
   def props = Props(classOf[DocumentRetriever])

   sealed trait DocumentRetrieverMessage

   case class RetrieveDocs(location: String) extends DocumentRetrieverMessage
}
