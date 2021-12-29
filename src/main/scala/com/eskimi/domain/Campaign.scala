package com.eskimi.domain

final case class Campaign(id: Int, country: String, targeting: Targeting, banners: List[Banner], bid: Double)
