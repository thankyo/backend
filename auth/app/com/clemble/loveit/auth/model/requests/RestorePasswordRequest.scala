package com.clemble.loveit.auth.model.requests

import com.clemble.loveit.common.error.FieldValidationError
import play.api.libs.json.Json

case class RestorePasswordRequest(
                                 password: String
                               ) {

  def validate() = {
    if (password.length > 65) throw FieldValidationError("password", "Max size is 64 characters")
  }

}

object RestorePasswordRequest {

  implicit val json = Json.format[RestorePasswordRequest]

}