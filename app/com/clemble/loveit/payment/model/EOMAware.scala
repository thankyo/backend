package com.clemble.loveit.payment.model

import java.time.YearMonth

import play.api.libs.json._

trait EOMAware {
  val yom: YearMonth
}

object EOMAware {

  //TODO switch to simpler presentation
  implicit val yomJsonFormat = new Format[YearMonth] {
    override def writes(o: YearMonth): JsValue = {
      Json.obj(
        "year" -> o.getYear,
        "month" -> o.getMonthValue
      )
    }

    override def reads(json: JsValue): JsResult[YearMonth] = {
      ((json \ "year").asOpt[Integer] -> (json \ "month").asOpt[Integer]) match {
        case (Some(year), Some(month)) => JsSuccess(YearMonth.of(year, month))
        case _ => JsError(s"Can't parse ${json} as YearMonth")
      }
    }
  }

}
