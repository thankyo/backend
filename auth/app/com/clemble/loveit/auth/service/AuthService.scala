package com.clemble.loveit.auth.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.auth.model.requests.{LogInRequest, RegisterRequest}
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.model.Email
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, CredentialsProvider, SocialProvider, SocialProviderRegistry}

import scala.concurrent.{ExecutionContext, Future}

sealed trait AuthServiceResult {
  val user: User
  val loginInfo: LoginInfo
}

case class UserRegister(user: User, loginInfo: LoginInfo) extends AuthServiceResult

case class UserLoggedIn(user: User, loginInfo: LoginInfo) extends AuthServiceResult

@Singleton
case class AuthService @Inject()(
                                  userService: UserService,
                                  avatarService: AvatarService,
                                  passwordHasherRegistry: PasswordHasherRegistry,
                                  authInfoRepository: AuthInfoRepository,
                                  credentialsProvider: CredentialsProvider,
                                  socialProviderRegistry: SocialProviderRegistry
                                )(
                                  implicit ec: ExecutionContext
                                ) {

  private def checkUserExists(email: Email): Future[Boolean] = {
    userService.findByEmail(email).flatMap {
      case Some(user) =>
        Future.failed(FieldValidationError("email", s"registered through social ${user.profiles.map(_.providerID)}"))
      case None =>
        Future.successful(false)
    }
  }

  def login(logIn: LogInRequest): Future[AuthServiceResult] = {
    credentialsProvider.authenticate(logIn.toCredentials()).flatMap { loginInfo =>
      userService.retrieve(loginInfo).flatMap {
        case Some(user) =>
          Future.successful(UserLoggedIn(user, loginInfo))
        case None =>
          checkUserExists(logIn.email).
            map(_ => throw FieldValidationError("email", "Email or Password is wrong"))
      }
    } recoverWith( {
      case t: Throwable if (t.isInstanceOf[InvalidPasswordException]) =>
        Future.failed(FieldValidationError("email", "Email or Password does not match"))
      case t: Throwable if (t.isInstanceOf[IdentityNotFoundException]) =>
        Future.failed(FieldValidationError("email", "This email was not registered"))
    })
  }

  def register(register: RegisterRequest): Future[AuthServiceResult] = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, register.email)
    authInfoRepository.find[PasswordInfo](loginInfo).flatMap {
      case Some(_) =>
        login(register.toLogIn()).
          recoverWith({ case _ => Future.failed(FieldValidationError("email", "already used")) })
      case None =>
        checkUserExists(register.email).
          flatMap(_ => {
            val authInfo = passwordHasherRegistry.current.hash(register.password)
            val user = register.toUser()
            for {
              avatar <- avatarService.retrieveURL(register.email)
              user <- userService.save(user.copy(avatar = avatar))
              _ <- authInfoRepository.save(loginInfo, authInfo)
            } yield {
              UserRegister(user, loginInfo)
            }
          })
    }
  }

  def registerSocial(p: SocialProvider with CommonSocialProfileBuilder)(authInfo: p.A): Future[AuthServiceResult] = {
    for {
      profile <- p.retrieveProfile(authInfo)
      eitherUser <- userService.createOrUpdateUser(profile)
      _ <- authInfoRepository.save(profile.loginInfo, authInfo)
    } yield {
      eitherUser match {
        case Left(user) => UserLoggedIn(user, profile.loginInfo)
        case Right(user) => UserRegister(user, profile.loginInfo)
      }
    }
  }

}
