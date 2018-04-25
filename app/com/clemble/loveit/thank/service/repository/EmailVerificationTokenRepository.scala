package com.clemble.loveit.thank.service.repository

import java.util.UUID

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.service.EmailVerificationToken

import scala.concurrent.Future

trait EmailVerificationTokenRepository {

  def findByToken(token: UUID): Future[Option[EmailVerificationToken]]

  def save(token: ResetPasswordToken): Future[EmailVerificationToken]

  def removeByToken(token: UUID): Future[Boolean]

  def removeByUser(user: UserID): Future[Boolean]


}
