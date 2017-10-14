package com.clemble.loveit.auth.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry, PasswordInfo}
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

  def login(credentials: Credentials): Future[AuthServiceResult] = {
    credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
      userService.retrieve(loginInfo).map {
        case Some(user) =>
          UserLoggedIn(user, loginInfo)
        case None =>
          throw new IllegalArgumentException("Couldn't find user")
      }
    }
  }

  def register(register: RegisterRequest): Future[AuthServiceResult] = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, register.email)
    authInfoRepository.find[PasswordInfo](loginInfo).flatMap {
      case Some(_) =>
        login(register.toCredentials()).
          recoverWith({ case _ => Future.failed(new IllegalArgumentException("Email already signedUp with a different password")) })
      case None =>
        userService.findByEmail(register.email).flatMap {
          case Some(user) =>
            Future.failed(new IllegalArgumentException(s"Email registered through social ${user.profiles.map(_.providerID)}"))
          case None =>
            val authInfo = passwordHasherRegistry.current.hash(register.password)
            val user = register.toUser()
            for {
              avatar <- avatarService.retrieveURL(register.email)
              user <- userService.save(user.copy(avatar = avatar))
              _ <- authInfoRepository.save(loginInfo, authInfo)
            } yield {
              UserRegister(user, loginInfo)
            }
        }
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
