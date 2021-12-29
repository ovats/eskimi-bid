package com.eskimi.samplebid.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.{BadRequest, NotFound}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{MissingQueryParamRejection, RejectionHandler, Route}
import com.eskimi.domain.{BidRequest, BidResponse}
import com.eskimi.samplebid.EskimiBid.logger
import com.eskimi.services.BidsService
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import org.slf4j.LoggerFactory

class BidRoutes(bidsService: BidsService) extends JacksonSupport {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val myRejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleNotFound {
        extractUnmatchedPath { p =>
          complete(NotFound, s"The path you requested [$p] does not exist.")
        }
      }
      .handle {
        case MissingQueryParamRejection(param) =>
          complete(BadRequest, s"Missing query Param error. $param")
        case a @ _ =>
          logger.info(s"some errors occurred here : $a")
          complete(BadRequest, s"Other error. $a")
      }
      .result()

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
