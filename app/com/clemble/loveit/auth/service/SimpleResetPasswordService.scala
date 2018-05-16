package com.clemble.loveit.auth.service

import java.util.UUID

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.auth.model.requests.{ResetPasswordRequest, RestorePasswordRequest}
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.service.{EmailService, TokenRepository, UserService}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import javax.inject.{Inject, Named, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
case class SimpleResetPasswordService @Inject()(
  userService: UserService,
  emailService: EmailService,
  passwordHasherRegistry: PasswordHasherRegistry,
  authInfoRepo: AuthInfoRepository,
  @Named("resetPasswordTokenRepo") repo: TokenRepository[ResetPasswordToken]
)(
  implicit
  ec: ExecutionContext
) extends ResetPasswordService {

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
      _ = if (!emailSent) throw new IllegalArgumentException("Could not send verification email")
    } yield {
      authToken
    }
  }

  override def restore(token: UUID, restore: RestorePasswordRequest): Future[UserLoggedIn] = {
    val passwordInfo = passwordHasherRegistry.current.hash(restore.password)
    for {
      authTokenOpt <- repo.findAndRemoveByToken(token)
      authToken = authTokenOpt.getOrElse({
        throw FieldValidationError("password", "Token expired or already used")
      })
      userOpt <- userService.findById(authToken.user)
      user = userOpt.getOrElse({ throw new IllegalArgumentException("User not found, this should never happen")})
      loginInfoOpt = user.profiles.asCredentialsLogin()
      _ <- authInfoRepo.update[PasswordInfo](loginInfoOpt.get, passwordInfo)
    } yield {
      UserLoggedIn(userOpt.get, loginInfoOpt.get)
    }
  }

}
