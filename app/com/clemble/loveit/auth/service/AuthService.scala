package com.clemble.loveit.auth.service

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.auth.model.requests.{LogInRequest, RegistrationRequest}
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.model.{Email, UserID}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.common.model.{CommonSocialProfileWithDOB, User}
import com.clemble.loveit.common.service.{UserOAuthService, UserService}
import com.mohiva.play.silhouette.api.{AuthInfo, LoginInfo}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers._

import scala.concurrent.{ExecutionContext, Future}

trait AuthService {

  def login(logIn: LogInRequest): Future[AuthServiceResult]

  def register(register: RegistrationRequest): Future[AuthServiceResult]

  def registerSocial(p: SocialProvider)(authInfo: p.A, userOpt: Option[UserID]): Future[AuthServiceResult]

  def removeSocial(user: UserID, provider: String): Future[Option[User]]

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

  override def register(register: RegistrationRequest): Future[AuthServiceResult] = {
    register.validate()

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

  override def registerSocial(p: SocialProvider)(authInfo: p.A, userOpt: Option[UserID]): Future[AuthServiceResult] = {
    for {
      profile <- p.retrieveProfile(authInfo)
      result <- createOrUpdateUser(profile, authInfo, userOpt)
    } yield {
      result
    }
  }

  private def createOrUpdateUser(profile: SocialProfile, authInfo: AuthInfo, userOpt: Option[UserID]): Future[AuthServiceResult] = {
    val profileEmail = profile match {
      case cs: CommonSocialProfile => cs.email
      case cswd: CommonSocialProfileWithDOB => cswd.email
      case _ => None
    }
    for {
      userById <- userOpt.map(user => userService.findById(user)).getOrElse(Future.successful(None))
      userByLogin <- userService.retrieve(profile.loginInfo)
      userByEmail <- profileEmail.map(userService.findByEmail).getOrElse(Future.successful(None))
      result <- userById.orElse(userByLogin).orElse(userByEmail) match {
        case Some(user: User) =>
          for {
            _ <- authInfoRepository.save(profile.loginInfo, authInfo)
            user <- userService.update(user link profile)
          } yield {
            UserLoggedIn(user, profile.loginInfo)
          }
        case _ =>
          val newUser = User(id = IDGenerator.generate(), email = profileEmail.get).link(profile)
          createUser(newUser, profile.loginInfo, authInfo)
      }
    } yield {
      result
    }
  }

  private def fetchAuthInfo(loginInfo: LoginInfo): Future[Option[AuthInfo]] = {
    authInfoRepository.find[AuthInfo](loginInfo).flatMap({
      case Some(info: OAuth2Info) if info.refreshToken.isDefined && AuthInfoUtils.hasExpired(info) => {
        val providerOpt = socialProviderRegistry.get[SocialProvider with RefreshableOAuth2Provider](loginInfo.providerID)
        providerOpt match {
          case Some(provider: RefreshableOAuth2Provider) =>
            for {
              refreshedInfo <- provider.refresh(info.refreshToken.get)
            } yield {
              authInfoRepository.update(loginInfo, refreshedInfo)
              Some(refreshedInfo)
            }
          case None =>
            Future.successful(Some(info))
        }
      }
      case other =>
        Future.successful(other)
    })
  }

  override def findAuthInfo(user: UserID, provider: String): Future[Option[AuthInfo]] = {
    for {
      userOpt <- userService.findById(user)
      loginInfoOpt = userOpt.flatMap(_.profiles.get(provider))
      authInfoOpt <- loginInfoOpt.map(fetchAuthInfo).getOrElse(Future.successful(None))
    } yield {
      authInfoOpt
    }
  }

  override def removeSocial(userId: UserID, provider: String): Future[Option[User]] = {
    userService.findById(userId).flatMap({
      case Some(user) if user.hasProvider(provider) =>
        val loginInfo = user.profiles.get(provider).get
        authInfoRepository.remove(loginInfo)
        userService.update(user.remove(provider)).map(Some(_))
      case res =>
        Future.successful(res)
    })
  }

}
