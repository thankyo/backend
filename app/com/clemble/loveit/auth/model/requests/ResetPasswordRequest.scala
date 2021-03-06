package com.clemble.loveit.auth.model.requests

import com.clemble.loveit.common.model.Email
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.libs.json.Json

case class ResetPasswordRequest(email: Email) {

  def toLoginInfo = {
    LoginInfo(CredentialsProvider.ID, email)
  }

}

object ResetPasswordRequest {

  implicit val json = Json.format[ResetPasswordRequest]

}
