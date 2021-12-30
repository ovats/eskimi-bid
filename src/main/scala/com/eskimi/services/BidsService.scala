package com.eskimi.services

import com.eskimi.domain.{BidRequest, BidResponse, Campaign, Device, Impression, User}
import com.eskimi.repository.Repository
import org.slf4j.LoggerFactory

//TODO remove if not needed
import java.time.LocalDateTime
import scala.collection.mutable.ListBuffer
import scala.util.Random

class BidsService(repo: Repository[Int, Campaign]) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private def matchCountry(country: String, device: Option[Device], user: Option[User]): Boolean = {
    val val1: Boolean = device.exists(_.geo.flatMap(_.country.map(_ == country)).getOrElse(false))
    val val2: Boolean = user.exists(_.geo.flatMap(_.country.map(_ == country)).getOrElse(false))
    val1 || val2
  }

  private def matchSite(siteList: Seq[String], siteId: String): Boolean = {
    siteList.contains(siteId)
  }

  def validateBid(bid: BidRequest): Option[BidResponse] = {

    //TODO replace ListBuffer by immutable data structures
    val resolvedResponses: ListBuffer[BidResponse] = ListBuffer.empty

    var withinBidImpr: Seq[Impression]            = Seq.empty
    val consideredBidImpr: ListBuffer[Impression] = ListBuffer.empty
    //TODO remove if not needed
//    val hourOfDay                                 = LocalDateTime.now().getHour
    val random = new Random()

    repo
      .findAll()
      .withFilter(c => matchCountry(c.country, bid.device, bid.user))
      .withFilter(c => matchSite(c.targeting.targetedSiteIds, bid.site.id))
      //TODO remove if not needed
//      .withFilter(c =>
//        c.targeting.startHourOfDay
//          .map(hourOfDay >= _)
//          .getOrElse(true) && c.targeting.endHourOfDay.map(hourOfDay <= _).getOrElse(true)
//      )
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
            "1",
            bid.id,
            impr.bidFloor.getOrElse(0.0d),
            Some(c.id.toString),
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
