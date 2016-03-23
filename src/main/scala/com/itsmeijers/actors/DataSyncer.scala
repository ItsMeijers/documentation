package com.itsmeijers.actors

import akka.actor._
import scala.concurrent.duration._
import DataSyncer._

class DataSyncer extends Actor with ActorLogging {

   // retrieve from config :)
   val scheduleTime = 1 hour

   // scheduler starts on startup of the actor
   implicit val dispatcher = context.dispatcher
   context.system.scheduler.schedule(1 second, scheduleTime, self, SyncData)

   // Actor that can retrieve the version of the documentation
   // val versionsRetriever = context.actorOf(VersionsRetriever.props, "versionsRetriever")

   // Actor that analayzes the api docs when it needs to update
   val apiDocAnalyst = context.actorOf(APIDocAnalyst.props, "apiDocAnalyst")

   /**
   * Messages receiver unkown case gets handled by ...
   */
   def receive = {
      case SyncData =>
         log.debug("Syncing data")
         println("Syncing data")
         //val (update, version) (versionRetriever ? VersionRetriever.RetrieveVersion)
         // if(update) {
            apiDocAnalyst ! APIDocAnalyst.StartAnalysis //(version)
         //}
      case unkown =>
         // handle unkown case
   }

}

object DataSyncer {

   def props = Props(classOf[DataSyncer])

   trait DataSyncerMessages
   case object SyncData extends DataSyncerMessages
}
