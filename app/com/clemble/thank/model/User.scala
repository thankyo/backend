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
                 owns: List[ResourceOwnership],
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

  /**
    * JSON format for [[User]]
    */
  implicit val jsonFormat = Json.format[User]

  def empty(uri: String) = {
    User(
      uri,
      uri,
      uri,
      List(ResourceOwnership.unrealized(uri)),
      0L,
      EmptyBankDetails,
      None,
      new DateTime(0)
    )
  }

}
