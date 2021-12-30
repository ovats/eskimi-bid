package com.eskimi.api.routes

import com.eskimi.domain.{Banner, BidRequest, Campaign, Device, Geo, Impression, Site, Targeting, User}

object TestData {

  val activeCampaigns: Seq[Campaign] = Seq(
    Campaign(
      id = 1,
      country = "LT",
      targeting = Targeting(
        targetedSiteIds = Seq("0006a522ce0f4bbbbaa6b3c38cafaa0f")
      ),
      banners = List(
        Banner(
          id = 1,
          src = "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
          width = 300,
          height = 250,
        )
      ),
      bid = 5d,
    )
  )

  val goodBidRequest: BidRequest =
    BidRequest(
      id = "SGu1Jpq110",
      site = Site(id = "0006a522ce0f4bbbbaa6b3c38cafaa0f", domain = "fake.tld"),
      device = Some(Device(id = "440579£46408831516ebd02f6e1c31b4", geo = Some(Geo(country = Some("LT"))))),
      imp = Some(
        List(
          Impression(
            id = "1",
            wmin = Some(50),
            wmax = Some(300),
            w = Some(300),
            h = Some(250),
            hmin = Some(100),
            hmax = Some(300),
            bidFloor = Some(3.12123d),
          )
        )
      ),
      user = Some(User(id = "USARI01", geo = Some(Geo(country = Some("LT"))))),
    )

  val bidRequestBadCountry: BidRequest =
    BidRequest(
      id = "SGu1Jpq110",
      site = Site(id = "0006a522ce0f4bbbbaa6b3c38cafaa0f", domain = "fake.tld"),
      device = Some(Device(id = "440579£46408831516ebd02f6e1c31b4", geo = Some(Geo(country = Some("US"))))),
      imp = Some(
        List(
          Impression(
            id = "1",
            wmin = Some(50),
            wmax = Some(300),
            w = Some(300),
            h = Some(250),
            hmin = Some(100),
            hmax = Some(300),
            bidFloor = Some(3.12123d),
          )
        )
      ),
      user = Some(User(id = "USARI01", geo = Some(Geo(country = Some("US"))))),
    )

  val bidRequestBadSite: BidRequest =
    BidRequest(
      id = "SGu1Jpq110",
      site = Site(id = "AAA6a522ce0f4bbbbaa6b3c38cafaa0f", domain = "fake.tld"),
      device = Some(Device(id = "440579£46408831516ebd02f6e1c31b4", geo = Some(Geo(country = Some("LT"))))),
      imp = Some(
        List(
          Impression(
            id = "1",
            wmin = Some(50),
            wmax = Some(300),
            w = Some(300),
            h = Some(250),
            hmin = Some(100),
            hmax = Some(300),
            bidFloor = Some(3.12123d),
          )
        )
      ),
      user = Some(User(id = "USARI01", geo = Some(Geo(country = Some("LT"))))),
    )

}
