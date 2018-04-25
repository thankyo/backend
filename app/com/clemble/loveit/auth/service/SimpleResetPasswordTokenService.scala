package com.clemble.loveit.auth.service

import java.util.UUID

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.auth.model.requests.ResetPasswordRequest
import com.clemble.loveit.common.error.{FieldValidationError}
import com.clemble.loveit.common.service.{TokenRepository, UserService}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
case class SimpleResetPasswordTokenService @Inject()(
  userService: UserService,
  emailService: EmailService,
  repo: TokenRepository[ResetPasswordToken]
)(
  implicit
  ec: ExecutionContext
) extends ResetPasswordTokenService {

  override def create(request: ResetPasswordRequest): Future[ResetPasswordToken] = {
    for {
      user <- userService.findByEmail(request.email).
          map({
            case Some(user) if user.profiles.credentials.isEmpty =>
              import user.profiles._
              val regStr = List(facebook.map(_ => "FB"), google.map(_ => "Google"), tumblr.map(_ => "Tumblr")).flatten.mkString(", ")
              throw FieldValidationError("email", s"You are registered through ${regStr}")
            case Some(user) => user
            case None =>
              throw new IdentityNotFoundException(s"No user with specified email exist")
          })
      authToken <- repo.save(ResetPasswordToken(user.id))
      emailSent <- emailService.sendResetPasswordEmail(user.email, authToken)
      _ = if (emailSent) throw new IllegalArgumentException("Could not send verification email")
    } yield {
      authToken
    }
  }

  def validate(token: UUID): Future[Option[ResetPasswordToken]] = {
    val findRes = repo.findByToken(token)
    repo.removeByToken(token)
    findRes
  }

}
