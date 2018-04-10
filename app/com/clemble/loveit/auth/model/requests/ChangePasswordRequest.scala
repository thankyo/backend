package com.clemble.loveit.auth.model.requests

import com.clemble.loveit.common.error.FieldValidationError
import play.api.libs.json.Json

case class ChangePasswordRequest(
  currentPassword: String,
  password: String
) {

  def validate() = FieldValidationError.validatePassword(password)

}

object ChangePasswordRequest {

  implicit val jsonFormat = Json.format[ChangePasswordRequest]

}