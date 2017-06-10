package com.clemble.loveit.payment

import java.time.YearMonth

import play.api.mvc.PathBindable

package object controller {

  implicit val stringToYearMonth: PathBindable[YearMonth] = new PathBindable[YearMonth] {

    override def bind(key: String, value: String): Either[String, YearMonth] = {
      val yom = value.split("/")
      if (yom.length == 2) {
        Right(YearMonth.of(yom(0).toInt, yom(1).toInt))
      } else {
        Left(value)
      }
    }

    override def unbind(key: String, value: YearMonth): String = {
      s"${value.getYear}/${value.getMonthValue}"
    }
  }

}
