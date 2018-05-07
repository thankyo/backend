package com.clemble.loveit.auth

import javax.inject.{Inject, Named, Singleton}

import com.clemble.loveit.common.service.{AuthyTwoFactoryProvider}
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto._
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.crypto.{JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api
import play.api.Configuration
import play.api.http.CookiesConfiguration
import play.api.libs.ws.WSClient
import play.api.mvc.{CookieHeaderEncoding, DefaultCookieHeaderEncoding}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

trait AdminAuthEnv extends Env{
  type I = AdminUser
  type A = CookieAuthenticator
}

case class AdminUser(name: String) extends Identity

@Singleton
case class AdminIdentityService @Inject() (wsClient: WSClient) extends IdentityService[AdminUser] {

  override def retrieve(loginInfo: LoginInfo): Future[Option[AdminUser]] = {
    loginInfo match {
      case LoginInfo(AuthyTwoFactoryProvider.ID, "admin") =>
        Future.successful(Some(AdminUser("admin")))
      case _ =>
        Future.successful(None)
    }
  }

}


class AdminSilhouetteModule(env: api.Environment, conf: Configuration) extends AbstractModule with ScalaModule {

  override def configure() {
    bind[Silhouette[AdminAuthEnv]].to[SilhouetteProvider[AdminAuthEnv]]
    bind[EventBus].annotatedWithName("adminEventBus")toInstance(EventBus())
  }

  @Provides
  @Singleton
  @Named("adminAuthSigner")
  def adminAuthSigner(): Signer = {
    val config = conf.underlying.as[JcaSignerSettings]("silhouette.admin.signer")
    new JcaSigner(config)
  }

  @Provides
  @Singleton
  @Named("adminHeaderEncoding")
  def adminHeaderEncoding(): CookieHeaderEncoding = {
    new DefaultCookieHeaderEncoding(CookiesConfiguration(true))
  }

  @Provides
  @Singleton
  def provideAuthenticatorService(
    @Named("adminAuthSigner") signer: Signer,
    @Named("adminHeaderEncoding") headerEncoding: CookieHeaderEncoding,
    clock: Clock,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    ec: ExecutionContext
  ): AuthenticatorService[CookieAuthenticator] = {

    val settings = conf.underlying.as[CookieAuthenticatorSettings]("silhouette.admin.authenticator")

    new CookieAuthenticatorService(
        settings,
        None,
        signer,
        headerEncoding,
        new Base64AuthenticatorEncoder(),
        fingerprintGenerator: FingerprintGenerator,
        idGenerator: IDGenerator,
        clock: Clock
    )(ec)
  }

  @Provides
  def provideEnvironment(
    adminSrc: AdminIdentityService,
    authenticatorService: AuthenticatorService[CookieAuthenticator],
    @Named("adminEventBus") eventBus: EventBus): Environment[AdminAuthEnv] = {

    Environment[AdminAuthEnv](
      adminSrc,
      authenticatorService,
      Seq(),
      eventBus
    )
  }


  @Provides
  def authyProvider(
    httpLayer: HTTPLayer
  )(implicit ec: ExecutionContext): AuthyTwoFactoryProvider = {

    new AuthyTwoFactoryProvider(
      conf.underlying.getString("silhouette.admin.apiKey"),
      httpLayer,
      ec
    )
  }

}
