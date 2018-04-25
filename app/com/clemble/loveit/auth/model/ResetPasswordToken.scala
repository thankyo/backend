package com.clemble.loveit.auth.model

import java.time.LocalDateTime
import java.util.UUID

import com.clemble.loveit.common.model.{CreatedAware, TokenAware, UserAware, UserID}
import play.api.libs.json.{Json, OFormat}

/**
 * A token to authenticate a user against an endpoint for a short time period.
 *
 * @param token The unique token ID.
 * @param user The unique ID of the user the token is associated with.
 */
case class ResetPasswordToken(
                      user: UserID,
                      token: UUID = UUID.randomUUID(),
                      created: LocalDateTime = LocalDateTime.now()
) extends TokenAware

object ResetPasswordToken {

  implicit val jsonFormat: OFormat[ResetPasswordToken] = Json.format[ResetPasswordToken]

}
