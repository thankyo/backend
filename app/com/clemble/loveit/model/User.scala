package com.clemble.loveit.model

import com.clemble.loveit.model.User.ExtendedBasicProfile
import com.clemble.loveit.payment.model.BankDetails
import com.clemble.loveit.util.IDGenerator
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfile}
import org.joda.time.{DateTime}
import play.api.libs.json.Json

trait UserProfile {
  val id: UserID
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
                 id: UserID,
                 firstName: Option[String] = None,
                 lastName: Option[String] = None,
                 owns: Set[ResourceOwnership] = Set.empty,
                 email: Option[Email] = None,
                 thumbnail: Option[String] = None,
                 bio: String = User.DEFAULT_BIO,
                 dateOfBirth: Option[DateTime] = None,
                 balance: Amount = 0L,
                 total: Amount = 0L,
                 bankDetails: BankDetails = BankDetails.empty,
                 profiles: Set[LoginInfo] = Set.empty,
                 created: DateTime = DateTime.now()
               ) extends Identity with UserProfile with CreatedAware {

  def assignOwnership(pendingBalance: Amount, resource: ResourceOwnership): User = {
    copy(balance = balance + pendingBalance, owns = owns + resource)
  }

  def increase(thanks: Int): User = {
    copy(
      balance = balance + thanks,
      total = total + thanks
    )
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

trait UserAware {
  val user: UserID
}

object User {

  val DEFAULT_BIO = "Mysterious Hero ;)"
  val DEFAULT_AMOUNT = 0L
  val DEFAULT_DATE_OF_BIRTH = new DateTime(0)

  implicit val socialProfileJsonFormat = Json.format[CommonSocialProfile]

  implicit val jsonFormat = Json.format[User]

  implicit class ExtendedBasicProfile(loginInfo: LoginInfo) {
    def toResource(): ResourceOwnership = {
      val uri = Resource.from(loginInfo)
      ResourceOwnership.full(uri)
    }
  }

  def from(profile: CommonSocialProfile): User = {
    User(IDGenerator.generate()).link(profile)
  }

  def empty(uri: Resource) = {
    User(
      id = IDGenerator.generate(),
      owns = Set(ResourceOwnership.unrealized(uri))
    )
  }

}
