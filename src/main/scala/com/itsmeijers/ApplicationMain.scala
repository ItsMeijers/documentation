package com.itsmeijers

import akka.actor.ActorSystem

import com.itsmeijers.actors._

object ApplicationMain extends App {
  val system = ActorSystem("DocumentationActorSystem")

  system.actorOf(DataSyncer.props, "dataSyncer")

  system.awaitTermination()
}
