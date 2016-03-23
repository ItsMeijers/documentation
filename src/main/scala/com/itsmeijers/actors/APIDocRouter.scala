package com.itsmeijers.actors

import akka.actor._
import java.io.File
import APIDocRouter._
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}

class APIDocRouter extends Actor with ActorLogging {

   val router = {
      val routees = Vector.fill(5) {
         val r = context.actorOf(DocumentScraper.props)
         context watch r
         ActorRefRoutee(r)
      }
      Router(RoundRobinRoutingLogic(), routees)
   }

   def receive = {
      case RouteFolder(folder) =>
         getFileTree(folder)
            .filter(f => """.*\.html$""".r.findFirstIn(f.getName).isDefined)
            .foreach { file =>
              router.route(DocumentScraper.ScrapeFile(file), context.self) // change to DocumentAuditor
            }
      case unkown =>
         // handle unkown
   }

   def getFileTree(f: File): Stream[File] = f #:: (
      if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
      else Stream.empty
   )

}

object APIDocRouter {

   def props = Props(classOf[APIDocRouter])

   sealed trait APIDocRouterMessage

   case class RouteFolder(folder: File)

}
