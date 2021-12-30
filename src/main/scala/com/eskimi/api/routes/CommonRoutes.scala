package com.eskimi.api.routes

import akka.http.scaladsl.model.StatusCodes.{BadRequest, NotFound}
import akka.http.scaladsl.server.Directives.{complete, extractUnmatchedPath}
import akka.http.scaladsl.server.{MissingQueryParamRejection, RejectionHandler}
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import org.slf4j.LoggerFactory

trait CommonRoutes extends JacksonSupport {

  private val logger = LoggerFactory.getLogger(this.getClass)

  val myRejectionHandler: RejectionHandler =
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
          logger.error(s"some errors occurred here : $a")
          complete(BadRequest, s"Other error. $a")
      }
      .result()

}
