package com.clemble.loveit.payment.controller

import java.time.YearMonth

import com.clemble.loveit.common.{ControllerSpec}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class YearMonthPathBindableSpec extends ControllerSpec {

  "read from string" in {
    val yom = someRandom[YearMonth]
    val readValue = YearMonthBindable.bind("some", s"${yom.getYear}/${yom.getMonthValue}")
    readValue shouldEqual Right(yom)
  }

  "write to string" in {
    val yom = someRandom[YearMonth]
    val writtenValue = YearMonthBindable.unbind("some", yom)
    writtenValue shouldEqual s"${yom.getYear}/${yom.getMonthValue}"
  }

}
