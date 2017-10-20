package com.clemble.loveit.auth.service

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.auth.model.AuthToken
import com.clemble.loveit.auth.service.repository.AuthTokenRepository
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.UserID

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Handles actions to auth tokens.
  *
  * @param repo The auth token Repository implementation.
  * @param ex   The execution context.
  */
@Singleton
case class SimpleAuthTokenService @Inject()(
                                             repo: AuthTokenRepository
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
  def create(userID: UserID): Future[AuthToken] = {
    val token = AuthToken(UUID.randomUUID(), userID)
    repo.
      save(token).
      recoverWith({
        case RepositoryException(RepositoryException.DUPLICATE_KEY_CODE, _) =>
          for {
            remove <- repo.removeByUser(userID) if (remove)
            savedToken <- repo.save(token)
          } yield {
            savedToken
          }
      })
  }

  /**
    * Validates a token ID.
    *
    * @param token The token ID to validate.
    * @return The token if it's valid, None otherwise.
    */
  def validate(token: UUID): Future[Option[AuthToken]] = {
    val findRes = repo.find(token)
    repo.remove(token)
    findRes
  }

}
