package com.clemble.thank.model

import com.clemble.thank.model.User.ExtendedBasicProfile
import com.clemble.thank.util.URIUtils
import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import securesocial.core._

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
                 firstName: Option[String] = None,
                 lastName: Option[String] = None,
                 owns: List[ResourceOwnership] = List.empty,
                 email: Option[Email] = None,
                 thumbnail: Option[String] = None,
                 dateOfBirth: Option[DateTime] = None,
                 balance: Amount = 0L,
                 bankDetails: BankDetails = EmptyBankDetails,
                 profiles: List[BasicProfile] = List.empty
               ) {

  def increase(thanks: Int): User = {
    copy(balance = balance + thanks)
  }

  def decrease(): User = {
    copy(balance = balance - 1)
  }

  def link(user: BasicProfile): User = {
    this.copy(
      firstName = firstName.orElse(user.firstName),
      lastName = lastName.orElse(user.lastName),
      email = email.orElse(user.email),
      thumbnail = thumbnail.orElse(user.avatarUrl),
      owns = (user.toResource() :: owns).distinct,
      profiles = (user :: profiles).distinct
    )
  }

  def findBySocialProfile(providerId: String, providerUserId: String): Option[BasicProfile] = {
    profiles.find(p => p.providerId == providerId && p.userId == providerUserId)
  }

}

object User {

  val DEFAULT_AMOUNT = 0L
  val DEFAULT_DATE_OF_BIRTH = new DateTime(0)

  /**
    * JSON format for [[User]]
    */
  implicit val authMethodFormat = Json.format[AuthenticationMethod]
  implicit val oauth1InfoFormat = Json.format[OAuth1Info]
  implicit val oauth2InfoFormat = Json.format[OAuth2Info]
  implicit val passwordInfoFormat = Json.format[PasswordInfo]
  implicit val basicProfileFormat = Json.format[BasicProfile]
  implicit val jsonFormat = Json.format[User]

  implicit class ExtendedBasicProfile(basicProfile: BasicProfile) {
    def toResource(): ResourceOwnership = {
      val uri = URIUtils.toUri(basicProfile)
      ResourceOwnership.full(uri)
    }
  }

  def fromProfile(user: BasicProfile): User = {
    User(BSONObjectID.generate().toString()).link(user)
  }

  def empty(uri: String) = {
    User(
      id = uri,
      owns = List(ResourceOwnership.unrealized(uri))
    )
  }

}
