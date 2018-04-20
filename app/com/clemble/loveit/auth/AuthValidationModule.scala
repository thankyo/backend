package com.clemble.loveit.auth

import javax.inject.Singleton
import akka.actor.ActorSystem
import com.clemble.loveit.auth.service.repository.ResetPasswordTokenRepository
import com.clemble.loveit.auth.service.repository.mongo.MongoResetPasswordTokenRepository
import com.clemble.loveit.auth.service._
import com.clemble.loveit.common.mongo.{JSONCollectionFactory}
import com.clemble.loveit.common.service.UserOAuthService
import com.clemble.loveit.common.util.{AuthEnv, EventBusManager}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.Environment
import net.codingwell.scalaguice.ScalaModule
import org.matthicks.mailgun.Mailgun
import play.api
import play.api.i18n.{MessagesApi, MessagesProvider}
import play.api.{Configuration, Mode}
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext

/**
  * The base Guice module.
  */
class AuthValidationModule(env: api.Environment, conf: Configuration) extends AbstractModule with ScalaModule {

  /**
    * Configures the module.
    */
  override def configure(): Unit = {
    bind[AuthService].to[SimpleAuthService]
    bind[UserOAuthService].to[SimpleAuthService]
    bind[ResetPasswordTokenRepository].to[MongoResetPasswordTokenRepository]
    bind[ResetPasswordTokenService].to[SimpleResetPasswordTokenService]
  }

  @Provides
  @Singleton
  @Named("resetToken")
  def authTokenCollection(collectionFactory: JSONCollectionFactory) = {
    collectionFactory.create("resetToken")
  }

  @Provides
  @Singleton
  def emailService(implicit provider: MessagesApi, ex: ExecutionContext): EmailService = {
    if (env.mode == Mode.Prod) {
      val apiKey = conf.get[String]("email.mailgun.api.key")
      val domain = conf.get[String]("email.mailgun.domain")
      MailgunEmailService(new Mailgun(domain, apiKey))
    } else {
      new StubEmailService()
    }
  }

  @Provides
  @Singleton
  def eventBusManager(env: Environment[AuthEnv], actorSystem: ActorSystem): EventBusManager = {
    EventBusManager(env, actorSystem)
  }

}
