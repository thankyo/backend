package com.clemble.loveit.auth.service

import java.util.UUID

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.common.model.UserID

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * Handles actions to auth tokens.
 */
trait ResetPasswordTokenService {

  /**
   * Creates a new auth token and saves it in the backing store.
   *
   * @param user The user ID for which the token should be created.
   * @return The saved auth token.
   */
  def create(user: UserID): Future[ResetPasswordToken]

  /**
   * Validates a token ID.
   *
   * @param token The token ID to validate.
   * @return The token if it's valid, None otherwise.
   */
  def validate(token: UUID): Future[Option[ResetPasswordToken]]

}
