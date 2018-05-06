package com.clemble.loveit.common.model

import java.time.{LocalDate, LocalDateTime}

import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.util.WriteableUtils
import com.mohiva.play.silhouette.api.Identity
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfile, CredentialsProvider, SocialProfile}
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import play.api.http.Writeable
import play.api.libs.json.{Json, OFormat}

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
  dateOfBirth: Option[LocalDate] = None,
  profiles: UserSocialConnections = UserSocialConnections(),
  created: LocalDateTime = LocalDateTime.now()
) extends Identity with CreatedAware {

  /**
    * Tries to construct a name.
    *
    * @return Maybe a name.
    */
  def name: Option[String] = firstName -> lastName match {
    case (Some(f), Some(l)) => Some(f + " " + l)
    case (_, _) => firstName.orElse(lastName)
  }

  def hasProvider(providerID: String): Boolean = profiles.get(providerID).isDefined

  def remove(providerID: String): User = {
    val userWithoutProvider = copy(profiles = profiles.remove(providerID))
    if (!userWithoutProvider.hasProvider(FacebookProvider.ID) && !userWithoutProvider.hasProvider(CredentialsProvider.ID))
      throw FieldValidationError(s"profiles.${providerID}", "You won't be able to login")
    userWithoutProvider
  }

  def link(socialProfile: SocialProfile): User = {
    val updatedProfiles = profiles.add(socialProfile.loginInfo)

    socialProfile match {
      case csp: CommonSocialProfile =>
        this.copy(
          firstName = firstName.orElse(csp.firstName),
          lastName = lastName.orElse(csp.lastName),
          profiles = updatedProfiles
        )
      case cspd: CommonSocialProfileWithDOB =>
        this.copy(
          firstName = firstName.orElse(cspd.firstName),
          lastName = lastName.orElse(cspd.lastName),
          profiles = updatedProfiles,
          dateOfBirth = cspd.dateOfBirth
        )
      case sp: SocialProfile =>
        this.copy(profiles = updatedProfiles)
    }
  }

  def clean(): User = {
    copy(email = "hidden@exampl.com", profiles = UserSocialConnections(), dateOfBirth = None)
  }

}

object User {

  val UNKNOWN = "UNKNOWN"

  implicit val socialProfileJsonFormat: OFormat[CommonSocialProfile] = Json.format[CommonSocialProfile]
  implicit val userSocialConnections: OFormat[UserSocialConnections] = Json.format[UserSocialConnections]
  implicit val jsonFormat: OFormat[User] = Json.format[User]

  implicit val userWriteable: Writeable[User] = WriteableUtils.jsonToWriteable[User]
  implicit val listUserWriteable: Writeable[List[User]] = WriteableUtils.jsonToWriteable[List[User]]

}