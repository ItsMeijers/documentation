package com.itsmeijers.actors

import akka.actor._
import APIDocAnalyst._
import scala.util.{Try, Success, Failure}
import java.io.File

class APIDocAnalyst extends Actor with ActorLogging {

   val apiDocRouter = context.actorOf(APIDocRouter.props, "apiDocRouter")

   var amountOfDocuments = 0 // Change into Map[Folder, Int]

   def receive = {
      case StartAnalysis =>
         log.debug("Starting Analysis")
         println("Starting Analaysis")
         startAnalysis()
      case unkown =>
         // handle unkown
   }

   def getFileTree(f: File): Stream[File] = f #:: (
      if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
      else Stream.empty
   )

   def startAnalysis() = {
      // scala = location + "/scala"
      // deprecetedFile = location + "/deprecated-list.html"
      // index = location + "/index"
      Try(new File(getClass.getResource("/scala-library/scala").toURI)) match {
        case Failure(e) =>
          println("****************failure!")
        case Success(f) => {
          apiDocRouter ! APIDocRouter.RouteFolder(f)
        }
      }
   }

}

object APIDocAnalyst {

   def props = Props(classOf[APIDocAnalyst])

   sealed trait APIDocAnalystMessage
   case object StartAnalysis extends APIDocAnalystMessage // add location

}
