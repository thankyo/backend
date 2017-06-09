package com.clemble.loveit.payment

import java.time.YearMonth

import play.api.libs.json._

/**
  * Created by mavarazy on 6/9/17.
  */
package object model {

  type StripeCustomerToken = String

  //TODO switch to simpler presentation
  implicit val yomJsonFormat = new Format[YearMonth] {
    override def writes(o: YearMonth): JsValue = {
      Json.obj(
        "year" -> o.getYear,
        "month" -> o.getMonthValue
      )
    }

    override def reads(json: JsValue): JsResult[YearMonth] = {
      ((json \ "year").asOpt[Int] -> (json \ "month").asOpt[Int]) match {
        case (Some(year), Some(month)) => JsSuccess(YearMonth.of(year, month))
        case _ => JsError(s"Can't parse ${json} as YearMonth")
      }
    }
  }

}
