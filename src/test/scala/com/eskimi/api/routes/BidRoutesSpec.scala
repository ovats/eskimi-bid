package com.eskimi.api.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.eskimi.common.BaseSpec
import com.eskimi.repository.InMemoryCampaignsRepo
import com.eskimi.services.BidsService

class BidRoutesSpec extends BaseSpec with ScalatestRouteTest {

  private val repo       = new InMemoryCampaignsRepo()
  private val bidService = new BidsService(repo)
  private val bidRoutes  = new BidRoutes(bidService)

  // Add some dummy data for unit tests
  TestData.activeCampaigns.foreach(repo.create)

  "POST /api/bid" should "return BadRequest when payload is empty" in {
    val request = Post(uri = s"/api/bid")
    request ~> bidRoutes.route ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  it should "return BadRequest when payload is incorrect/invalid" in {
    val request = Post(uri = s"/api/bid", "adasdfsdfdf")
    request ~> bidRoutes.route ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  it should "return Ok when payload is valid/correct" in {
    val request = Post(uri = s"/api/bid", TestData.goodBidRequest)
    request ~> bidRoutes.route ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  it should "return NoContent when payload is valid/correct but country doesn't match" ignore {
    val request = Post(uri = s"/api/bid", TestData.bidRequestBadCountry)
    request ~> bidRoutes.route ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  it should "return NoContent when payload is valid/correct but site doesn't match" ignore {
    val request = Post(uri = s"/api/bid", TestData.bidRequestBadSite)
    request ~> bidRoutes.route ~> check {
      status shouldBe StatusCodes.OK
    }
  }

}
