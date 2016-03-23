package com.itsmeijers.actors

import akka.actor._
import DocumentParser._
import org.jsoup.nodes.Document
import com.itsmeijers.models.DocumentationItems._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import org.jsoup.nodes.Element
import cats.syntax.flatMap._
import cats.data.Reader

class DocumentParser extends Actor with ActorLogging {

   type Work[A] = Reader[Element, A]

   implicit def toReader[A](f: Element => A): Work[A] = Reader(f)

   def receive = {
      case ParseDocument(doc) =>
         val parsedDocument = parseDocument(doc)
   }

   def parseDocument(document: Document) = {
      for {
         (title, docType, packages) <- findTitleDocTypePackages
      } yield DocItem(title, docType, packages, List())

      // document >?> element("#definition") map { definition =>
      //    val docItem = for {
      //       title <- findTitle
      //       docType <- findDocType
      //       packages <- findPackages
      //    } yield DocItem(title, docType, packages)
      //
      //    println(docItem.run(definition).toString)
      // } // else index
   }

   def findTitleDocTypePackages: Work[(String, DocumentationType, List[String])] =
      (elm: Element) => {
         val values = for{
            title <- findTitle
            docType <- findDocType
            packages <- findPackages
         } yield (title, docType, packages)

         values.run(elm >> element("#owner"))
      }



   def findPackages: Work[List[String]] = (elm: Element) =>
      (elm >> element("#owner")
                  >> elements(".extype")).map(_ >> text("a")).toList

   def findTitle: Work[String] = (elm: Element) => (elm >> text("h1"))


   def findDocType: Work[DocumentationType] = (elm: Element) =>
      (elm >> element("img")).attr("alt").split('/').head match {
         case "Trait" => Trait
         case "Class" => Class
         case "Object" => Object
         case "Package" => Package
      }

   def find
}

object DocumentParser {

   def props = Props(classOf[DocumentParser])

   sealed trait DocumentParserMessage

   case class ParseDocument(doc: Document) extends DocumentParserMessage

}
