package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.user.model.UserAware
import play.api.libs.json.Json

case class ContributionStatistics(
  user: UserID,
  contributions: Int
) extends UserAware

object ContributionStatistics {

  def empty(user: UserID) = ContributionStatistics(user, 0)

  implicit val jsonFormat = Json.format[ContributionStatistics]
  implicit val writeable = WriteableUtils.jsonToWriteable[ContributionStatistics]

}
