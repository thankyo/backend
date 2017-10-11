package com.clemble.loveit.auth.service

import java.util.UUID
import javax.inject.Inject

import com.clemble.loveit.auth.model.AuthToken
import com.clemble.loveit.auth.service.repository.AuthTokenRepository
import com.clemble.loveit.common.model.UserID
import com.mohiva.play.silhouette.api.util.Clock

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
 * Handles actions to auth tokens.
 *
 * @param repo The auth token Repository implementation.
 * @param clock        The clock instance.
 * @param ex           The execution context.
 */
class SimpleAuthTokenService @Inject()(
                                        repo: AuthTokenRepository,
                                        clock: Clock
)(
  implicit
  ex: ExecutionContext
) extends AuthTokenService {

  /**
   * Creates a new auth token and saves it in the backing store.
   *
   * @param userID The user ID for which the token should be created.
   * @return The saved auth token.
   */
  def create(userID: UserID) = {
    val token = AuthToken(UUID.randomUUID(), userID)
    repo.save(token)
  }

  /**
   * Validates a token ID.
   *
   * @param id The token ID to validate.
   * @return The token if it's valid, None otherwise.
   */
  def validate(id: UUID): Future[Option[AuthToken]] = {
    repo.find(id)
  }

}
