package com.clemble.loveit.user

import com.clemble.loveit.common.util.{AuthEnv, TestSocialProvider}
import com.clemble.loveit.user.service.repository.UserRepository
import javax.inject.Singleton

import com.google.inject.Provides
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import com.mohiva.play.silhouette.impl
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorService, JWTAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.state.DummyStateProvider
import com.mohiva.play.silhouette.impl.providers.{OAuth2Settings, OAuth2StateProvider, SocialProviderRegistry}
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

class SocialModule extends ScalaModule {

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
                   userService: UserRepository,
                   authenticatorService: AuthenticatorService[JWTAuthenticator],
                   eventBus: EventBus,
                   ec: ExecutionContext): Environment[AuthEnv] = {
    Environment[AuthEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )(ec)
  }

  @Provides
  @Singleton
  def facebookProvider(
                        httpLayer: HTTPLayer,
                        stateProvider: OAuth2StateProvider,
                        configuration: Configuration
                      ): FacebookProvider = {
    val facebookConfig = configuration.underlying.as[OAuth2Settings]("silhouette.facebook")
    new FacebookProvider(httpLayer, stateProvider, facebookConfig)
  }

  @Provides
  @Singleton
  def testProvider(
                    httpLayer: HTTPLayer,
                    stateProvider: OAuth2StateProvider,
                    configuration: Configuration
                  ): TestSocialProvider = {
    val testConfig = new OAuth2Settings(
      accessTokenURL = "",
      redirectURL = "",
      clientID = "",
      clientSecret = ""
    )
    new TestSocialProvider(httpLayer, stateProvider, testConfig)
  }

  @Provides
  @Singleton
  def socialProviderRegistry(env: api.Environment, fp: FacebookProvider, tp: TestSocialProvider): SocialProviderRegistry = {
    val providers = if (env.mode == Mode.Prod) Seq(fp) else Seq(fp, tp)
    SocialProviderRegistry(providers)
  }

  @Provides
  @Singleton
  def idGenerator(ec: ExecutionContext): IDGenerator = {
    new SecureRandomIDGenerator()(ec)
  }

  @Provides
  @Singleton
  def crypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.jwt.authenticator.crypter")
    new JcaCrypter(config)
  }

  @Provides
  @Singleton
  def provideAuthenticatorService(
                                   crypter: Crypter,
                                   idGenerator: IDGenerator,
                                   configuration: Configuration,
                                   clock: Clock,
                                   ec: ExecutionContext): AuthenticatorService[JWTAuthenticator] = {

    val config = configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.jwt.authenticator.jwt")
    val encoder = new CrypterAuthenticatorEncoder(crypter)

    new JWTAuthenticatorService(config, None, encoder, idGenerator, clock)(ec)
  }

  @Provides
  @Singleton
  def oAuth2StateProvider(): OAuth2StateProvider = {
    new DummyStateProvider()
  }

}
