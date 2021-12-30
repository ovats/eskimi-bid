package com.eskimi.api.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.eskimi.domain.{BidRequest, BidResponse}
import com.eskimi.services.BidsService
import org.slf4j.LoggerFactory

class BidRoutes(bidsService: BidsService) extends CommonRoutes {

  private val logger = LoggerFactory.getLogger(this.getClass)

  val route: Route =
    handleRejections(myRejectionHandler) {
      (path("api" / "bid") & post) {
        entity(as[BidRequest]) { bid =>
          logger.info(s">>>>>>>>>>>>>>>> ----------------------------------------------------<<<<<<<<<<<<<<")
          val response: Option[BidResponse] = bidsService.validateBid(bid)
          logger.info(s">>>>>>>>> response: $response")
          response match {
            case Some(b) => complete(b)
            case None    => complete(StatusCodes.NoContent)
          }
        }
      }

    }

}
