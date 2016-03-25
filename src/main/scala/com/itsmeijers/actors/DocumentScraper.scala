package com.itsmeijers.actors

import akka.actor._
import java.io.File
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import DocumentScraper._
import org.jsoup.nodes.Document
import com.itsmeijers.models.DocumentationItems._
import com.itsmeijers.scraper.DocumentationItemScraper

class DocumentScraper extends Actor with ActorLogging {

   val browser = new Browser

   val scraper = DocumentationItemScraper.scrapeDocumentationItem

   def receive = {
      case ScrapeFile(file) =>
         val document = browser.parseFile(file)
         val result = scraper.run(document)
         //println(s"$result")
      case unkown =>
         // handle unknown
   }

}

object DocumentScraper {
   def props: Props = Props(classOf[DocumentScraper])

   sealed trait DocumentScraperMessage
   case class ScrapeFile(file: File)
}
