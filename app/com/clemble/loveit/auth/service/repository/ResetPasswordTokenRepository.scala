package com.clemble.loveit.auth.service.repository

import java.util.UUID

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.common.model.UserID

import scala.concurrent.Future

/**
 * Give access to the [[ResetPasswordToken]] object.
 */
trait ResetPasswordTokenRepository {

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID): Future[Option[ResetPasswordToken]]

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: ResetPasswordToken): Future[ResetPasswordToken]

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Boolean]

  def removeByUser(user: UserID): Future[Boolean]
}
