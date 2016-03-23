package com.itsmeijers.actors

import akka.actor._
import DocumentParser._
import org.jsoup.nodes.Document
import com.itsmeijers.models.DocumentationItems._
import com.itsmeijers.scraper.DocumentationItemScraper

class DocumentParser extends Actor with ActorLogging {

   def receive = {
      case ParseDocument(doc) =>
        val test = DocumentationItemScraper.scrapeSignature.run(doc)
        println(s"Signature: $test")
   }

}

object DocumentParser {

   def props = Props(classOf[DocumentParser])

   sealed trait DocumentParserMessage

   case class ParseDocument(doc: Document) extends DocumentParserMessage

}
