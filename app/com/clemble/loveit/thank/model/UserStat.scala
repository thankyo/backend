package com.clemble.loveit.thank.model

import java.time.YearMonth

import com.clemble.loveit.common.model.UserID
import play.api.libs.json._

case class UserStat(
                     id: UserID,
                     yearMonth: YearMonth,
                     total: Int = 0
)

object UserStat {

  implicit val yearMonthJsonFormat = new Format[YearMonth] {
    override def reads(json: JsValue): JsResult[YearMonth] = {
      JsSuccess(YearMonth.of((json \ "year").as[Int], (json \ "month").as[Int]))
    }

    override def writes(o: YearMonth): JsValue = {
      Json.obj("year" -> o.getYear, "month" -> o.getMonthValue)
    }
  }

  implicit val jsonFormat = Json.format[UserStat]

}
