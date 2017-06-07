package com.clemble.loveit.user.model

import com.clemble.loveit.common.model._
import com.clemble.loveit.payment.model.{BankDetails, Money, ThankTransaction, UserPayment}
import com.clemble.loveit.thank.model.{ROVerification, UserResource}
import com.clemble.loveit.common.util.{IDGenerator, WriteableUtils}
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.joda.time.DateTime
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
                 owns: Set[Resource] = Set.empty,
                 verification: Option[ROVerification[Resource]] = None,
                 email: Option[Email] = None,
                 thumbnail: Option[String] = None,
                 bio: String = User.DEFAULT_BIO,
                 dateOfBirth: Option[DateTime] = None,
                 balance: Amount = 0L,
                 pending: List[ThankTransaction] = List.empty,
                 total: Amount = 0L,
                 bankDetails: Option[BankDetails] = None,
                 monthlyLimit: Money = UserPayment.DEFAULT_LIMIT,
                 profiles: Set[LoginInfo] = Set.empty,
                 created: DateTime = DateTime.now()
               ) extends Identity with UserProfile with CreatedAware with UserPayment with UserResource {

  def assignOwnership(resource: Resource): User = {
    copy(owns = owns + resource)
  }

  def link(socialProfile: CommonSocialProfile): User = {
    this.copy(
      firstName = firstName.orElse(socialProfile.firstName),
      lastName = lastName.orElse(socialProfile.lastName),
      email = email.orElse(socialProfile.email),
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

  implicit val userWriteable = WriteableUtils.jsonToWriteable[User]

  def from(profile: CommonSocialProfile): User = {
    User(IDGenerator.generate()).link(profile)
  }

}
