package com.clemble.loveit.auth.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.auth.model.requests.{LogInRequest, RegisterRequest}
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.model.{Email, UserID}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api.{AuthInfo, LoginInfo}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers._

import scala.concurrent.{ExecutionContext, Future}

trait AuthService {

  def login(logIn: LogInRequest): Future[AuthServiceResult]

  def register(register: RegisterRequest): Future[AuthServiceResult]

  def registerSocial(p: SocialProvider with CommonSocialProfileBuilder)(authInfo: p.A, userOpt: Option[UserID]): Future[AuthServiceResult]

}

@Singleton
case class SimpleAuthService @Inject()(
                                  userService: UserService,
                                  avatarService: AvatarService,
                                  passwordHasherRegistry: PasswordHasherRegistry,
                                  authInfoRepository: AuthInfoRepository,
                                  credentialsProvider: CredentialsProvider,
                                  socialProviderRegistry: SocialProviderRegistry
                                )(
                                  implicit ec: ExecutionContext
                                ) extends AuthService with UserOAuthService {

  private def checkUserExists(email: Email): Future[Boolean] = {
    userService.findByEmail(email).flatMap {
      case Some(user) =>
        Future.failed(FieldValidationError("email", s"registered through FB ${user.profiles.facebook.isDefined} or Google ${user.profiles.google.isDefined}"))
      case None =>
        Future.successful(false)
    }
  }

  override def login(logIn: LogInRequest): Future[AuthServiceResult] = {
    credentialsProvider.authenticate(logIn.toCredentials()).flatMap { loginInfo =>
      userService.retrieve(loginInfo).flatMap {
        case Some(user) =>
          Future.successful(UserLoggedIn(user, loginInfo))
        case None =>
          checkUserExists(logIn.email).
            map(_ => throw FieldValidationError("email", "No user found please, we'll investigate this"))
      }
    }
  }

  override def register(register: RegisterRequest): Future[AuthServiceResult] = {
    val loginInfo = register.toLoginInfo()
    authInfoRepository.find[PasswordInfo](loginInfo).flatMap {
      case Some(_) =>
        login(register.toLogIn()).
          recoverWith({ case _ => Future.failed(FieldValidationError("email", "already used")) })
      case None =>
        checkUserExists(register.email).
          flatMap(_ => {
            val authInfo = passwordHasherRegistry.current.hash(register.password)
            val user = register.toUser()
            createUser(user, loginInfo, authInfo)
          })
    }
  }

  private def createUser(user: User, loginInfo: LoginInfo, authInfo: AuthInfo): Future[UserRegister] = {
    for {
      avatar <- avatarService.retrieveURL(user.email)
      user <- userService.create(user.copy(avatar = avatar))
      _ <- authInfoRepository.save(loginInfo, authInfo)
    } yield {
      UserRegister(user, loginInfo)
    }
  }

  override def registerSocial(p: SocialProvider with CommonSocialProfileBuilder)(authInfo: p.A, userOpt: Option[UserID]): Future[AuthServiceResult] = {
    for {
      profile <- p.retrieveProfile(authInfo)
      result <- createOrUpdateUser(profile, authInfo, userOpt)
    } yield {
      result
    }
  }

  private def createOrUpdateUser(profile: CommonSocialProfile, authInfo: AuthInfo, userOpt: Option[UserID]): Future[AuthServiceResult] = {
    for {
      userById <- userOpt.map(user => userService.findById(user)).getOrElse(Future.successful(None))
      userByLogin <- userService.retrieve(profile.loginInfo)
      userByEmail <- profile.email.map(userService.findByEmail).getOrElse(Future.successful(None))
      result <- userById.orElse(userByLogin).orElse(userByEmail) match {
        case Some(user: User) => {
          for {
            _ <- authInfoRepository.save(profile.loginInfo, authInfo)
            user <- userService.update(user link profile)
          } yield {
            UserLoggedIn(user, profile.loginInfo)
          }
        }
        case _ => {
          val newUser = User(id = IDGenerator.generate(), email = profile.email.get).link(profile)
          createUser(newUser, profile.loginInfo, authInfo)
        }
      }
    } yield {
      result
    }
  }

  override def findAuthInfo(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    authInfoRepository.find[OAuth2Info](loginInfo)
  }

}
