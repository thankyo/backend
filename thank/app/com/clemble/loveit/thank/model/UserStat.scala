package com.clemble.loveit.thank.model

import java.time.YearMonth

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.model.yearMonthJsonFormat

import play.api.libs.json._

case class UserStat(
                     id: UserID,
                     yearMonth: YearMonth,
                     total: Int = 0
)

object UserStat {

  implicit val jsonFormat = Json.format[UserStat]

}
