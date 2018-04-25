package com.clemble.loveit.auth.service

import java.util.UUID

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.service.TokenRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Handles actions to auth tokens.
  *
  * @param repo The auth token Repository implementation.
  * @param ex   The execution context.
  */
@Singleton
case class SimpleResetPasswordTokenService @Inject()(
                                             repo: TokenRepository[ResetPasswordToken]
                                           )(
                                             implicit
                                             ex: ExecutionContext
                                           ) extends ResetPasswordTokenService {

  /**
    * Creates a new auth token and saves it in the backing store.
    *
    * @param user The user ID for which the token should be created.
    * @return The saved auth token.
    */
  def create(user: UserID): Future[ResetPasswordToken] = {
    val token = ResetPasswordToken(UUID.randomUUID(), user)
    repo.
      save(token).
      recoverWith({
        case RepositoryException(RepositoryException.DUPLICATE_KEY_CODE, _) =>
          for {
            _ <- repo.removeByUser(user)
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
  def validate(token: UUID): Future[Option[ResetPasswordToken]] = {
    val findRes = repo.findByToken(token)
    repo.removeByToken(token)
    findRes
  }

}
