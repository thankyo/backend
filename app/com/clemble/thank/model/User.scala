package com.clemble.thank.model

import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * User abstraction
  *
  * User has few roles
  * - Gratitude giver
  *    - give thanks, to different urls
  * - Gratitude receiver
  *    - receives thanks from owned urls
  */
case class User(
                 id: UserId,
                 firstName: String,
                 lastName: String,
                 url: String,
                 integrations: List[Integration],
                 balance: Amount,
                 bankDetails: BankDetails,
                 email: Option[Email],
                 dateOfBirth: DateTime
) {

  def increase(thanks: Int): User = {
    copy(balance = balance + thanks)
  }

  def decrease(): User = {
    copy(balance = balance - 1)
  }

}

object User {

  implicit val json = Json.format[User]

}
