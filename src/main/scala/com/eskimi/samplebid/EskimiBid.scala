package com.eskimi.samplebid

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.eskimi.config.AppConfig
import com.eskimi.domain._
import com.eskimi.samplebid.routes.BidRoutes
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import org.slf4j.LoggerFactory

import java.time.LocalDateTime
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.Random

object EskimiBid extends JacksonSupport {

  val logger = LoggerFactory.getLogger(this.getClass)

  def constantCampaigns(): Seq[Campaign] = {
    Seq[Campaign](
      Campaign(
        id = 1,
        country = "LT",
        targeting =
          Targeting(Seq("0006a522ce0f4bbbbaa6b3c38cafaa0f"), startHourOfDay = Some(8), endHourOfDay = Some(21)),
        banners = List(
          Banner(1, "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg", 300, 250),
          Banner(2, "https://business.eskimi.com/wp-content/uploads/2020/07/openGraph.jpeg", 300, 250),
        ),
        bid = 4.50,
      ),
      Campaign(
        id = 2,
        country = "LT",
        targeting = Targeting(Seq("0006a522ce0f4bbbbaa6b3c38cafaa0f"), endHourOfDay = Some(21)),
        banners = List(
          Banner(1, "https://home.eskimi.com/wp-content/uploads/2021/01/openGraph.jpeg", 250, 100),
          Banner(2, "https://home.eskimi.com/wp-content/uploads/2021/01/openGraph.jpeg", 275, 110),
        ),
        bid = 5.15,
      ),
      Campaign(id = 3, country = "NG", targeting = Targeting(Seq.empty[String]), banners = Nil, bid = 2.15),
    )
  }

  def validateBid(bid: BidRequest)(implicit campaigns: Seq[Campaign]): Option[BidResponse] = {
    //TODO replace ListBuffer by immutable data structures
    val resolvedResponses: ListBuffer[BidResponse] = ListBuffer.empty

    var withinBidImpr: Seq[Impression]            = Seq.empty
    val consideredBidImpr: ListBuffer[Impression] = ListBuffer.empty
    val hourOfDay                                 = LocalDateTime.now().getHour
    val random                                    = new Random()

    campaigns
      .withFilter(c =>
        bid.device.exists(_.geo.flatMap(_.country.map(_ == c.country)).getOrElse(true)) || bid.user.exists(
            _.geo.flatMap(_.country.map(_ == c.country)).getOrElse(true)
          )
      )
      .withFilter(_.targeting.targetedSiteIds.contains(bid.site.id))
      .withFilter(c =>
        c.targeting.startHourOfDay
          .map(hourOfDay >= _)
          .getOrElse(true) && c.targeting.endHourOfDay.map(hourOfDay <= _).getOrElse(true)
      )
      .withFilter(c =>
        bid.imp match {
          case None => false
          case Some(lstimpr) =>
            withinBidImpr = lstimpr.filter(_.bidFloor.exists(_ <= c.bid))
            withinBidImpr.nonEmpty
        }
      )
      .foreach { c =>
        val resolved_banners = c.banners.filter(bnf =>
          withinBidImpr.map { imp =>
            val _exists =
              (imp.w.exists(_ >= bnf.width) || (imp.wmax.exists(_ >= bnf.width) && imp.wmin.exists(_ <= bnf.width))) &&
                (imp.h
                  .exists(_ >= bnf.height) || (imp.hmax.exists(_ >= bnf.height) && imp.hmin.exists(_ <= bnf.height)))
            if (_exists) consideredBidImpr += imp
            _exists
          }.nonEmpty
        )

        if (consideredBidImpr.nonEmpty) {
          var _rand  = random.nextInt(resolved_banners.size)
          val banner = resolved_banners(_rand)

          _rand = random.nextInt(consideredBidImpr.size)
          val impr = consideredBidImpr(_rand)
          resolvedResponses += BidResponse(
            1.toString,
            bid.id,
            impr.bidFloor.getOrElse(0.0d),
            Some(s"${c.id}"),
            Some(banner),
          )
        }

        //clear temp
        consideredBidImpr.clear
        withinBidImpr = Seq.empty
      }

    if (resolvedResponses.nonEmpty) {
      resolvedResponses.zipWithIndex.foreach { case (bid, c) => logger.info(s"$c: $bid") }
      val _rand = random.nextInt(resolvedResponses.length)
      Some(resolvedResponses(_rand))
    } else {
      None
    }
  }

  implicit def myRejectionHandler: RejectionHandler =
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

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[Nothing]               = ActorSystem(Behaviors.empty, "EskimiBid")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    implicit val campaigns: Seq[Campaign] = DataGenerator.sampleCampaigns(5, None, 3.0, 5.5, Some(6), None)
    campaigns.zipWithIndex.foreach { case (cam, c) => logger.info(s"${c + 1}: $cam") }

    // Config of the API
    val config = AppConfig()
    val host   = config.api.host
    val port   = config.api.port

    // Routes of the API
    val route      = new BidRoutes().route
    val httpserver = Http().newServerAt(host, port).bind(route)

    logger.info(s"Server now online. Please send request to http://$host:$port/api/bid\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    httpserver
      .flatMap(_.unbind())                 // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}
