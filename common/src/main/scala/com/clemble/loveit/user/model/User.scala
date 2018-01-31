package com.clemble.loveit.user.model

import java.time.{LocalDate, LocalDateTime}

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.WriteableUtils
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
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
                 profiles: Set[LoginInfo] = Set.empty,
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

  def hasProvider(providerID: String): Boolean = {
    profiles.exists(_.providerID == providerID)
  }

  def link(socialProfile: CommonSocialProfile): User = {
    this.copy(
      firstName = firstName.orElse(socialProfile.firstName),
      lastName = lastName.orElse(socialProfile.lastName),
      // following #60 we should ignore avatar from profile for now
      profiles = profiles + socialProfile.loginInfo
    )
  }

}

trait UserAware {
  val user: UserID
}

object User {

  val UNKNOWN = "UNKNOWN"

  implicit val socialProfileJsonFormat: OFormat[CommonSocialProfile] = Json.format[CommonSocialProfile]
  implicit val jsonFormat: OFormat[User] = Json.format[User]

  implicit val userWriteable: Writeable[User] = WriteableUtils.jsonToWriteable[User]

}
