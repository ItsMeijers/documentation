package com.itsmeijers.actors

import akka.actor._
import akka.routing.{ ActorRefRoutee, RoundRobinRoutingLogic, Router }
import ParserScheduler._
import org.jsoup.nodes.Document

class ParserScheduler(val documentRetriever: ActorRef) extends Actor with ActorLogging {

   var documentsToParse = 0
   var documentsRouted = 0
   var documentsParsed = 0

   val router = {
      val routees = Vector.fill(5) {
         val r = context.actorOf(DocumentParser.props)
         context watch r
         ActorRefRoutee(r)
      }
      Router(RoundRobinRoutingLogic(), routees)
   }

   def receive = {
      case ParseDocuments(location) =>
         documentRetriever ! DocumentRetriever.RetrieveDocs(location)
      case RouteParseDocument(doc) =>
         router.route(DocumentParser.ParseDocument(doc), context.self)
         documentsRouted += 1
      case AmountOfDocuments(amount) =>
         documentsToParse = amount
   }
}

object ParserScheduler {

   def props(documentRetriever: ActorRef) = Props(classOf[ParserScheduler], documentRetriever)

   sealed trait ParserSchedulerMessage

   case class ParseDocuments(location: String) extends ParserSchedulerMessage
   case class RouteParseDocument(document: Document) extends ParserSchedulerMessage
   case class AmountOfDocuments(amount: Int) extends ParserSchedulerMessage
}
