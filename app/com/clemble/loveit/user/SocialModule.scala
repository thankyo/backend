package com.clemble.loveit.user

import com.clemble.loveit.common.util.{AuthEnv, TestSocialProvider}
import com.clemble.loveit.user.service.repository.UserRepository
import javax.inject.Singleton

import akka.actor.{ActorSystem, Props}
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.{SubscriptionManager, SubscriptionOnSignUpManager, UserService}
import com.google.inject.Provides
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorService, JWTAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import com.mohiva.play.silhouette.impl.providers.state.{CsrfStateItemHandler, CsrfStateSettings}
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, _}
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import com.mohiva.play.silhouette.persistence.daos.InMemoryAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api
import play.api.libs.ws.WSClient
import play.api.{Configuration, Mode}

import scala.concurrent.ExecutionContext
import net.ceedubs.ficus.readers.EnumerationReader._

class SocialModule(env: api.Environment, conf: Configuration) extends ScalaModule {

  override def configure(): Unit = {
    bind[Clock].toInstance(Clock())
    bind[Silhouette[AuthEnv]].to[SilhouetteProvider[AuthEnv]]
  }

  @Provides
  @Singleton
  def authInfoRepository(ec: ExecutionContext): AuthInfoRepository = {
    new DelegableAuthInfoRepository(new InMemoryAuthInfoDAO[impl.providers.OAuth2Info]())(ec)
  }

  @Provides
  @Singleton
  def httpLayer(client: WSClient, ec: ExecutionContext): HTTPLayer = {
    new PlayHTTPLayer(client)(ec)
  }

  @Provides
  @Singleton
  def environment(
                   system: ActorSystem,
                   userService: UserService,
                   subscriptionManager: SubscriptionManager,
                   userRepo: UserService,
                   authenticatorService: AuthenticatorService[JWTAuthenticator],
                   eventBus: EventBus,
                   ec: ExecutionContext): Environment[AuthEnv] = {

    val signUpSubscription = system.actorOf(Props(SubscriptionOnSignUpManager(userService, subscriptionManager)))
    eventBus.subscribe(signUpSubscription, classOf[SignUpEvent[User]])

    Environment[AuthEnv](
      userRepo,
      authenticatorService,
      Seq(),
      eventBus
    )(ec)
  }

  @Provides
  @Singleton
  def facebookProvider(
                        httpLayer: HTTPLayer,
                        stateProvider: SocialStateHandler
                      ): FacebookProvider = {
    val facebookConfig = conf.underlying.as[OAuth2Settings]("silhouette.facebook")
    new FacebookProvider(httpLayer, stateProvider, facebookConfig)
  }

  @Provides
  @Singleton
  def testProvider(
                    httpLayer: HTTPLayer,
                    stateHandler: SocialStateHandler,
                    stateProvider: SocialStateHandler
                  ): TestSocialProvider = {
    val testConfig = new OAuth2Settings(
      accessTokenURL = "",
      clientID = "",
      clientSecret = ""
    )
    new TestSocialProvider(httpLayer, stateProvider, stateHandler, testConfig)
  }

  @Provides
  @Singleton
  def socialProviderRegistry(fp: FacebookProvider, tp: TestSocialProvider): SocialProviderRegistry = {
    val providers = if (env.mode == Mode.Prod) Seq(fp) else Seq(fp, tp)
    SocialProviderRegistry(providers)
  }

  @Provides
  @Singleton
  def crypter(): Crypter = {
    val config = conf.underlying.as[JcaCrypterSettings]("silhouette.jwt.authenticator.crypter")
    new JcaCrypter(config)
  }

  @Provides
  @Singleton
  def signer(): Signer = {
    val config = conf.underlying.as[JcaSignerSettings]("silhouette.jwt.authenticator.signer")
    new JcaSigner(config)
  }

  @Provides
  @Singleton
  def provideAuthenticatorService(
                                   crypter: Crypter,
                                   clock: Clock,
                                   ec: ExecutionContext): AuthenticatorService[JWTAuthenticator] = {

    val config = conf.underlying.as[JWTAuthenticatorSettings]("silhouette.jwt.authenticator.jwt")
    val encoder = new CrypterAuthenticatorEncoder(crypter)

    new JWTAuthenticatorService(config, None, encoder, new SecureRandomIDGenerator()(ec), clock)(ec)
  }

  @Provides
  @Singleton
  def csrfStateSettings(): CsrfStateSettings = {
    CsrfStateSettings()
  }

  @Provides
  @Singleton
  def oAuth2StateProvider(handler: CsrfStateItemHandler, signer: Signer): SocialStateHandler = {
    new DefaultSocialStateHandler(Set(), signer)
  }

}
