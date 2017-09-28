package com.clemble.loveit.auth.models.services

import java.util.UUID

import com.clemble.loveit.auth.models.AuthToken
import com.clemble.loveit.common.model.UserID

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * Handles actions to auth tokens.
 */
trait AuthTokenService {

  /**
   * Creates a new auth token and saves it in the backing store.
   *
   * @param userID The user ID for which the token should be created.
   * @return The saved auth token.
   */
  def create(userID: UserID): Future[AuthToken]

  /**
   * Validates a token ID.
   *
   * @param id The token ID to validate.
   * @return The token if it's valid, None otherwise.
   */
  def validate(id: UUID): Future[Option[AuthToken]]

}
