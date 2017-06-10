package com.clemble.loveit.payment.controller

import java.time.YearMonth

import com.clemble.loveit.common.ThankSpecification

class YearMonthPathBindableSpec extends ThankSpecification {

  "read from string" in {
    val yom = someRandom[YearMonth]
    val readValue = stringToYearMonth.bind("some", s"${yom.getYear}/${yom.getMonth}")
    readValue shouldEqual Left(yom)
  }

  "write to string" in {
    val yom = someRandom[YearMonth]
    val writtenValue = stringToYearMonth.unbind("some", yom)
    writtenValue shouldEqual s"${yom.getYear}/${yom.getMonth}"
  }

}
