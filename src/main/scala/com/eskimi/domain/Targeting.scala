package com.eskimi.domain

final case class Targeting(
    targetedSiteIds: Seq[String],
    startHourOfDay: Option[Int] = None,
    endHourOfDay: Option[Int] = None,
)
