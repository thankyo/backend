package com.clemble.loveit.user

import com.clemble.loveit.user.service._
import com.clemble.loveit.user.service.repository._
import com.clemble.loveit.user.service.repository.mongo.{MongoInvitationRepository, MongoUserRepository}
import javax.inject.{Named, Singleton}

import com.clemble.loveit.common.mongo.JSONCollectionFactory
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule
import org.joda.time.DateTimeZone
import play.api.libs.ws.WSClient
import play.api.{Configuration, Environment, Mode}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext

/**
  * Module with all service dependencies
  */
class UserModule(env: Environment, conf: Configuration) extends ScalaModule {

  override def configure(): Unit = {
    DateTimeZone.setDefault(DateTimeZone.UTC)

    bind(classOf[UserService]).to(classOf[SimpleUserService])
    bind(classOf[UserRepository]).to(classOf[MongoUserRepository])

    bind(classOf[InvitationRepository]).to(classOf[MongoInvitationRepository])
    bind(classOf[InvitationService]).to(classOf[SimpleInvitationService])
  }

  @Provides
  @Named("user")
  @Singleton
  def userMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("user", mongoApi, ec, env)
  }

  @Provides
  @Named("invitation")
  @Singleton
  def inviteMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("invitation", mongoApi, ec, env)
  }


  @Provides
  @Singleton
  def subscriptionManager(ws: WSClient, ec: ExecutionContext): SubscriptionManager = {
    if (env.mode == Mode.Test) {
      TestSubscriptionManager
    } else {
      val apiKey = conf.get[String]("email.mailgun.api.key")
      MailgunSubscriptionManager(apiKey, ws, ec)
    }
  }



}
