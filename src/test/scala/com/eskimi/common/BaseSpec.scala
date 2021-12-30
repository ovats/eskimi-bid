package com.eskimi.common

import de.heikoseeberger.akkahttpjackson.JacksonSupport
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

trait BaseSpec extends AnyFlatSpecLike with Matchers with JacksonSupport
