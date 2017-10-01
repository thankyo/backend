package com.clemble.loveit.user.model

import java.time.LocalDateTime

import com.clemble.loveit.auth.models.requests.SignUpRequest
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.{IDGenerator, WriteableUtils}
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfile, CredentialsProvider}
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
                 id: UserID,
                 firstName: Option[String] = None,
                 lastName: Option[String] = None,
                 email: Email,
                 avatar: Option[String] = None,
                 bio: Option[String] = None,
                 dateOfBirth: Option[LocalDateTime] = None,
                 profiles: Set[LoginInfo] = Set.empty,
                 created: LocalDateTime = LocalDateTime.now()
               ) extends Identity with CreatedAware {

  /**
    * Tries to construct a name.
    *
    * @return Maybe a name.
    */
  def name = firstName -> lastName match {
    case (Some(f), Some(l)) => Some(f + " " + l)
    case (_, _) => firstName.orElse(lastName)
  }

  def hasProvider(providerID: String): Boolean = {
    profiles.exists(_.providerID == providerID)
  }

  def link(socialProfile: CommonSocialProfile): User = {
    this.copy(
      firstName = firstName.orElse(socialProfile.firstName),
      lastName = lastName.orElse(socialProfile.lastName),
      // following #60
      avatar = socialProfile.avatarURL.orElse(avatar),
      profiles = profiles + socialProfile.loginInfo
    )
  }

}

trait UserAware {
  val user: UserID
}

object User {

  val DEFAULT_AMOUNT = 0L

  implicit val socialProfileJsonFormat = Json.format[CommonSocialProfile]
  implicit val jsonFormat = Json.format[User]

  implicit val userWriteable = WriteableUtils.jsonToWriteable[User]

  def from(profile: CommonSocialProfile): User = {
    val email = profile.email.get
    User(id = IDGenerator.generate(), email = email).
      link(profile)
  }

  def from(signUp: SignUpRequest): User = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, signUp.email)
    new User(
      id = IDGenerator.generate(),
      firstName = Some(signUp.firstName),
      lastName = Some(signUp.lastName),
      email = signUp.email,
      avatar = None,
      profiles = Set(loginInfo)
    )
  }

}
