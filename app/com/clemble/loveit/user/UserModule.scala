package com.clemble.loveit.user

import com.clemble.loveit.user.service._
import com.clemble.loveit.user.service.repository._
import com.clemble.loveit.user.service.repository.mongo.{MongoInvitationRepository, MongoUserRepository}
import javax.inject.{Named, Singleton}
import akka.actor.ActorSystem
import com.clemble.loveit.common.mongo.JSONCollectionFactory
import com.clemble.loveit.common.service.UserService
import com.clemble.loveit.common.util.{AuthEnv, EventBusManager}
import com.google.inject.Provides
import com.mohiva.play.silhouette.api
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.WSClient
import play.api.{Configuration, Mode, Environment => PlayEnvironment}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext

/**
  * Module with all service dependencies
  */
class UserModule(env: PlayEnvironment, conf: Configuration) extends ScalaModule {

  override def configure(): Unit = {
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
  def subscriptionManager(ws: WSClient, ec: ExecutionContext, eventBusManager: EventBusManager): SubscriptionManager = {
    if (env.mode == Mode.Test) {
      TestSubscriptionManager
    } else {
      val apiKey = conf.get[String]("email.mailgun.api.key")
      MailgunSubscriptionManager(apiKey, eventBusManager, ws, ec)
    }
  }



}
