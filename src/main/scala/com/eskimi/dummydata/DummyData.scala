package com.eskimi.dummydata

import com.eskimi.domain.{Banner, Campaign, Targeting}
import com.eskimi.repository.Repository

object DummyData {

  def addDummyCampaigns(repo: Repository[Int, Campaign]): Unit = {

    repo.create(
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
      )
    )

    repo.create(
      Campaign(
        id = 2,
        country = "LT",
        targeting = Targeting(Seq("0006a522ce0f4bbbbaa6b3c38cafaa0f"), endHourOfDay = Some(21)),
        banners = List(
          Banner(1, "https://home.eskimi.com/wp-content/uploads/2021/01/openGraph.jpeg", 250, 100),
          Banner(2, "https://home.eskimi.com/wp-content/uploads/2021/01/openGraph.jpeg", 275, 110),
        ),
        bid = 5.15,
      )
    )

    repo.create(
      Campaign(id = 3, country = "NG", targeting = Targeting(Seq.empty[String]), banners = Nil, bid = 2.15)
    )
  }
}
