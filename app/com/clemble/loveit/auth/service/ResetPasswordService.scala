package com.clemble.loveit.auth.service

import java.util.UUID

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.auth.model.requests.{ResetPasswordRequest, RestorePasswordRequest}

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * Handles actions to auth tokens.
 */
trait ResetPasswordService {

  def create(request: ResetPasswordRequest): Future[ResetPasswordToken]

  def restore(token: UUID, restore: RestorePasswordRequest): Future[UserLoggedIn]

}
