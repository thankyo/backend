package com.clemble.loveit.auth

import javax.inject.Singleton
import com.clemble.loveit.auth.service._
import com.clemble.loveit.auth.service.repository.mongo.MongoAuthInfoRepository
import com.clemble.loveit.common.mongo.{JSONCollectionFactory}
import com.clemble.loveit.common.service.{TumblrProvider, UserService}
import com.clemble.loveit.common.util.AuthEnv
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.actions.{DefaultSecuredErrorHandler, DefaultUnsecuredErrorHandler, SecuredErrorHandler, UnsecuredErrorHandler}
import com.mohiva.play.silhouette.api.crypto._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{CookieSecretProvider, CookieSecretSettings}
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, _}
import com.mohiva.play.silhouette.impl.providers.oauth2._
import com.mohiva.play.silhouette.impl.services._
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import net.ceedubs.ficus.readers.EnumerationReader._
import play.modules.reactivemongo.ReactiveMongoApi

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule(env: api.Environment, conf: Configuration) extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  override def configure() {
    bind[Silhouette[AuthEnv]].to[SilhouetteProvider[AuthEnv]]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())

    bind[UnsecuredErrorHandler].to[DefaultUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[DefaultSecuredErrorHandler]

    // Replace this with the bindings to your concrete DAOs
    bind[AuthInfoRepository].to(classOf[MongoAuthInfoRepository])
  }

  @Provides
  @Singleton
  @Named("cookieCrypter")
  def cookieCrypter(): Crypter = {
    val config = conf.underlying.as[JcaCrypterSettings]("silhouette.cookie.crypter")
    new JcaCrypter(config)
  }

  @Provides
  @Singleton
  @Named("authInfo")
  def authInfoCollection(collectionFactory: JSONCollectionFactory) = {
    collectionFactory.create("authInfo")
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

  /**
   * Provides the HTTP layer implementation.
   *
   * @param client Play's WS client.
   * @return The HTTP layer implementation.
   */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
   * Provides the Silhouette environment.
   *
   * @param userService The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
                          userService: UserService,
                          authenticatorService: AuthenticatorService[JWTAuthenticator],
                          eventBus: EventBus): Environment[AuthEnv] = {

    Environment[AuthEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }


  @Provides @Named("oauth1-token-secret-signer")
  def oAuth1TokenSecretSigner(): Signer = {
    val config = conf.underlying.as[JcaSignerSettings]("silhouette.oauth1TokenSecretProvider.signer")

    new JcaSigner(config)
  }

  @Provides @Named("oauth1-token-secret-crypter")
  def oAuth1TokenSecretCrypter(): Crypter = {
    val config = conf.underlying.as[JcaCrypterSettings]("silhouette.oauth1TokenSecretProvider.crypter")

    new JcaCrypter(config)
  }

  @Provides
  def oAuth1TokenSecretProvider(
    @Named("oauth1-token-secret-signer") signer: Signer,
    @Named("oauth1-token-secret-crypter") crypter: Crypter,
    clock: Clock): CookieSecretProvider = {

    val settings = conf.underlying.as[CookieSecretSettings]("silhouette.oauth1TokenSecretProvider")
    new CookieSecretProvider(settings, signer, crypter, clock)
  }

  /**
   * Provides the social provider registry.
   *
   * @param facebookProvider The Facebook provider implementation.
   * @return The Silhouette environment.
   */
  @Provides
  def provideSocialProviderRegistry(
    facebookProvider: FacebookProviderWithDOB,
    googleProvider: GoogleProvider,
    tumblrProvider: TumblrProvider
  ): SocialProviderRegistry = {

    SocialProviderRegistry(Seq(
      facebookProvider,
      googleProvider,
      tumblrProvider
    ))
  }

  /**
   * Provides the signer for the CSRF state item handler.
   * .
   * @return The signer for the CSRF state item handler.
   */
  @Provides @Named("csrf-state-item-signer")
  def provideCSRFStateItemSigner(): Signer = {
    val config = conf.underlying.as[JcaSignerSettings]("silhouette.csrfStateItemHandler.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the signer for the social state handler.
   *
   * @return The signer for the social state handler.
   */
  @Provides @Named("social-state-signer")
  def provideSocialStateSigner(): Signer = {
    val config = conf.underlying.as[JcaSignerSettings]("silhouette.socialStateHandler.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the avatar service.
   *
   * @param httpLayer The HTTP layer implementation.
   * @return The avatar service implementation.
   */
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = {
    new GravatarService(httpLayer, GravatarServiceSettings(true, Map("s" -> "200", "d" -> "identicon")))
  }

  /**
   * Provides the password hasher registry.
   *
   * @return The password hasher registry.
   */
  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq())
  }

  /**
   * Provides the credentials provider.
   *
   * @param authInfoRepository The auth info repository implementation.
   * @param passwordHasherRegistry The password hasher registry.
   * @return The credentials provider.
   */
  @Provides
  def provideCredentialsProvider(
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry): CredentialsProvider = {
    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }


  @Provides
  def provideSocialStateHandler(signer: Signer):SocialStateHandler = {
    new DefaultSocialStateHandler(Set.empty, signer)
  }

  @Provides
  def facebookProvider(
    httpLayer: HTTPLayer,
    socialStateHandler: SocialStateHandler)
    ( implicit ec: ExecutionContext): FacebookProviderWithDOB = {

    new FacebookProviderWithDOB(
      httpLayer,
      socialStateHandler,
      conf.underlying.as[OAuth2Settings]("silhouette.facebook")
    )
  }

  @Provides
  def googleProvider(httpLayer: HTTPLayer, socialStateHandler: SocialStateHandler): GoogleProvider with RefreshableOAuth2Provider = {
    new GoogleProvider(
      httpLayer,
      socialStateHandler,
      conf.underlying.as[OAuth2Settings]("silhouette.google")
    ) with GoogleRefreshableProvider
  }

  @Provides
  def tumblrProvider(httpLayer: HTTPLayer, tokenSecretProvider: CookieSecretProvider): TumblrProvider = {
    val settings = conf.underlying.as[OAuth1Settings]("silhouette.tumblr")
    new TumblrProvider(
      httpLayer,
      new PlayOAuth1Service(settings),
      tokenSecretProvider,
      settings
    )
  }
}
