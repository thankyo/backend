package com.clemble.loveit.common.model

import java.time.LocalDate

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfile, SocialProfile}

case class CommonSocialProfileWithDOB(
  loginInfo: LoginInfo,
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  fullName: Option[String] = None,
  email: Option[String] = None,
  avatarURL: Option[String] = None,
  dateOfBirth: Option[LocalDate]
) extends SocialProfile {

  def this(csp: CommonSocialProfile, dob: Option[LocalDate]) = this(
    loginInfo = csp.loginInfo,
    firstName = csp.firstName,
    lastName = csp.lastName,
    fullName = csp.fullName,
    email = csp.email,
    avatarURL = csp.avatarURL,
    dob
  )
}
