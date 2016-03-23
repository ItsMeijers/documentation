package com.itsmeijers

import akka.actor.ActorSystem

import com.itsmeijers.actors._

object ApplicationMain extends App {
  val system = ActorSystem("DocumentationActorSystem")

  val documentRetriever = system.actorOf(DocumentRetriever.props, "documentRetriever")

  val parserScheduler = system.actorOf(ParserScheduler.props(documentRetriever), "parserScheduler")

  // change location that it takes scala-library and later on it splits it in index, deprecated-list, scala
  val location = "/scala-library/scala"

  parserScheduler ! ParserScheduler.ParseDocuments(location)

  // This example app will ping pong 3 times and thereafter terminate the ActorSystem -
  // see counter logic in PingActor
  system.awaitTermination()
}
