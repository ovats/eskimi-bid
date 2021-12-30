package com.eskimi.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.eskimi.config.AppConfig
import com.eskimi.domain._
import com.eskimi.repository.InMemoryCampaignsRepo
import com.eskimi.dummydata.DataGenerator
import com.eskimi.api.routes.BidRoutes
import com.eskimi.services.BidsService
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object EskimiBid {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[Nothing]               = ActorSystem(Behaviors.empty, "EskimiBid")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    // Config of the API
    val config = AppConfig()
    val host   = config.api.host
    val port   = config.api.port

    // Repositories and services
    val campaignsRepo = new InMemoryCampaignsRepo()
    val bidsService   = new BidsService(campaignsRepo)

    // Dummy data
    val campaigns: Seq[Campaign] = DataGenerator.sampleCampaigns(5, None, 3.0, 5.5, Some(6), None)
    campaigns.foreach(campaignsRepo.create)

    // Routes of the API
    val route      = new BidRoutes(bidsService).route
    val httpserver = Http().newServerAt(host, port).bind(route)

    logger.info(s"Server now online. Please send request to http://$host:$port/api/bid\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    httpserver
      .flatMap(_.unbind())                 // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}
