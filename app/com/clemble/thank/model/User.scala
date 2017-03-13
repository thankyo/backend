package com.clemble.thank.model

import com.clemble.thank.model.User.ExtendedBasicProfile
import com.clemble.thank.util.URIUtils
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfile, SocialProfile}
import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID

trait UserProfile {
  val id: UserId
  val firstName: Option[String]
  val lastName: Option[String]
  val thumbnail: Option[String]
  val dateOfBirth: Option[DateTime]
}

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
                 owns: Set[ResourceOwnership] = Set.empty,
                 email: Option[Email] = None,
                 thumbnail: Option[String] = None,
                 dateOfBirth: Option[DateTime] = None,
                 balance: Amount = 0L,
                 bankDetails: BankDetails = EmptyBankDetails,
                 profiles: Set[LoginInfo] = Set.empty
               ) extends Identity with UserProfile {

  def increase(thanks: Int): User = {
    copy(balance = balance + thanks)
  }

  def decrease(): User = {
    copy(balance = balance - 1)
  }

  def link(socialProfile: CommonSocialProfile): User = {
    this.copy(
      firstName = firstName.orElse(socialProfile.firstName),
      lastName = lastName.orElse(socialProfile.lastName),
      email = email.orElse(socialProfile.email),
      owns = owns + socialProfile.loginInfo.toResource(),
      thumbnail = thumbnail.orElse(socialProfile.avatarURL),
      profiles = profiles + socialProfile.loginInfo
    )
  }

  def toIdentity(): UserIdentity = {
    UserIdentity(
      id,
      firstName,
      lastName,
      thumbnail,
      dateOfBirth
    )
  }

}

object User {

  val DEFAULT_AMOUNT = 0L
  val DEFAULT_DATE_OF_BIRTH = new DateTime(0)

  implicit val format = Json.format[User]

  implicit class ExtendedBasicProfile(basicProfile: LoginInfo) {
    def toResource(): ResourceOwnership = {
      val uri = URIUtils.toUri(basicProfile)
      ResourceOwnership.full(uri)
    }
  }

  def from(profile: CommonSocialProfile): User = {
    User(BSONObjectID.generate().stringify).link(profile)
  }

  def empty(uri: String) = {
    User(
      id = uri,
      owns = Set(ResourceOwnership.unrealized(uri))
    )
  }

}
