package com.clemble.loveit.auth.model.requests

import com.clemble.loveit.common.model.Email
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.libs.json.{Json, OFormat}

case class RegisterRequest(
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

  def toLogIn(): LogInRequest = {
    LogInRequest(email, password)
  }
}

object RegisterRequest {

  implicit val json: OFormat[RegisterRequest] = Json.format[RegisterRequest]

}