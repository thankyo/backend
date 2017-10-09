package com.clemble.loveit.payment

import java.time.YearMonth

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.model.Resource.from
import play.api.libs.json._
import play.api.mvc.PathBindable

package object model {

  type StripeCustomerToken = String

  //TODO switch to simpler presentation
  implicit val yomJsonFormat: Format[YearMonth] = new Format[YearMonth] {
    override def writes(o: YearMonth): JsValue = {
      Json.obj(
        "year" -> o.getYear,
        "month" -> o.getMonthValue
      )
    }

    override def reads(json: JsValue): JsResult[YearMonth] = {
      (json \ "year").asOpt[Int] -> (json \ "month").asOpt[Int] match {
        case (Some(year), Some(month)) => JsSuccess(YearMonth.of(year, month))
        case _ => JsError(s"Can't parse ${json} as YearMonth")
      }
    }
  }

}
