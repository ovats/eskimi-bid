package com.eskimi.services

import com.eskimi.domain.{BidRequest, BidResponse, Campaign, Impression}
import com.eskimi.repository.Repository
import com.eskimi.samplebid.EskimiBid.logger

import java.time.LocalDateTime
import scala.collection.mutable.ListBuffer
import scala.util.Random

class BidsService(repo: Repository[Int, Campaign]) {

  def validateBid(bid: BidRequest): Option[BidResponse] = {

    //TODO replace ListBuffer by immutable data structures
    val resolvedResponses: ListBuffer[BidResponse] = ListBuffer.empty

    var withinBidImpr: Seq[Impression]            = Seq.empty
    val consideredBidImpr: ListBuffer[Impression] = ListBuffer.empty
    val hourOfDay                                 = LocalDateTime.now().getHour
    val random                                    = new Random()

    //campaigns
    repo
      .findAll()
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

}
