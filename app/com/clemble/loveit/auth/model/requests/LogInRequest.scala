package com.clemble.loveit.auth.model.requests

import com.clemble.loveit.common.model.Email
import com.mohiva.play.silhouette.api.util.Credentials
import play.api.libs.json.{Json, OFormat}

case class LogInRequest(email: Email, password: String) {

  def toCredentials() = Credentials(email, password)

}

object LogInRequest {

  implicit val jsonFormat: OFormat[LogInRequest] = Json.format[LogInRequest]

}