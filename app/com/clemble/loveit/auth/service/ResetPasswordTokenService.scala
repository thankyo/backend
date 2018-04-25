package com.clemble.loveit.auth.service

import java.util.UUID

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.auth.model.requests.ResetPasswordRequest

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * Handles actions to auth tokens.
 */
trait ResetPasswordTokenService {

  def create(request: ResetPasswordRequest): Future[ResetPasswordToken]

  def validate(token: UUID): Future[Option[ResetPasswordToken]]

}
