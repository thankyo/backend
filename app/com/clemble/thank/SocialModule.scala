package com.clemble.thank

import com.clemble.thank.service.repository.UserRepository
import com.clemble.thank.util.AuthEnv
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto.{CookieSigner, Crypter, CrypterAuthenticatorEncoder}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCookieSigner, JcaCookieSignerSettings, JcaCrypter, JcaCrypterSettings}
import com.mohiva.play.silhouette.impl
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorService, JWTAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{CookieStateProvider, CookieStateSettings}
import com.mohiva.play.silhouette.impl.providers.{OAuth2Settings, OAuth2StateProvider, SocialProviderRegistry}
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import com.mohiva.play.silhouette.persistence.daos.InMemoryAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

class SocialModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[Clock].toInstance(Clock())
    bind[Silhouette[AuthEnv]].to[SilhouetteProvider[AuthEnv]]
  }

  @Provides
  def authInfoRepository(ec: ExecutionContext): AuthInfoRepository = {
    new DelegableAuthInfoRepository(new InMemoryAuthInfoDAO[impl.providers.OAuth2Info]())(ec)
  }

  @Provides
  def httpLayer(client: WSClient, ec: ExecutionContext): HTTPLayer = {
    new PlayHTTPLayer(client)(ec)
  }

  @Provides
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
  def facebookProvider(
                               httpLayer: HTTPLayer,
                               stateProvider: OAuth2StateProvider,
                               configuration: Configuration
                             ): FacebookProvider = {
    val facebookConfig = configuration.underlying.as[OAuth2Settings]("silhouette.facebook")
    new FacebookProvider(httpLayer, stateProvider, facebookConfig)
  }

  @Provides
  def socialProviderRegistry(fp: FacebookProvider): SocialProviderRegistry = {
    SocialProviderRegistry(Seq(fp))
  }

  @Provides
  def idGenerator(ec: ExecutionContext): IDGenerator = {
    new SecureRandomIDGenerator()(ec)
  }

  @Provides
  def crypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.jwt.authenticator.crypter")
    new JcaCrypter(config)
  }

  @Provides
  def provideAuthenticatorService(
                                   crypter: Crypter,
                                   idGenerator: IDGenerator,
                                   configuration: Configuration,
                                   clock: Clock,
                                   ec: ExecutionContext): AuthenticatorService[JWTAuthenticator] = {

    val config = JWTAuthenticatorSettings(sharedSecret = "changeme")
    val encoder = new CrypterAuthenticatorEncoder(crypter)

    new JWTAuthenticatorService(config, None, encoder, idGenerator, clock)(ec)
  }

  @Provides
  def cookieSigner(configuration: Configuration): CookieSigner = {
    val config = configuration.underlying.as[JcaCookieSignerSettings]("silhouette.oauth2StateProvider.cookie.signer")

    new JcaCookieSigner(config)
  }

  @Provides
  def oAuth2StateProvider(
                                  idGenerator: IDGenerator,
                                  cookieSigner: CookieSigner,
                                  configuration: Configuration,
                                  clock: Clock): OAuth2StateProvider = {
    val settings = configuration.underlying.as[CookieStateSettings]("silhouette.oauth2StateProvider")
    new CookieStateProvider(settings, idGenerator, cookieSigner, clock)
  }

}
