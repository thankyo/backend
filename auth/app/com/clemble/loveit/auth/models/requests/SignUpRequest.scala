package com.clemble.loveit.auth.models.requests

import com.clemble.loveit.common.model.Email
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.libs.json.{Json, OFormat}

case class SignUpRequest(
                          firstName: String,
                          lastName: String,
                          email: Email,
                          password: String
                        ) {

  def toUser(): User = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, email)
    User(
      id = IDGenerator.generate(),
      firstName = Some(firstName),
      lastName = Some(lastName),
      email = email,
      avatar = None,
      profiles = Set(loginInfo)
    )
  }
}

object SignUpRequest {

  implicit val json: OFormat[SignUpRequest] = Json.format[SignUpRequest]

}