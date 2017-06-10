package com.clemble.loveit.user

import com.clemble.loveit.user.service._
import com.clemble.loveit.user.service.repository._
import com.clemble.loveit.user.service.repository.mongo.MongoUserRepository
import javax.inject.{Inject, Named, Singleton}

import akka.actor.ActorSystem
import com.clemble.loveit.common.mongo.JSONCollectionFactory
import com.clemble.loveit.common.util.AuthEnv
import com.google.inject.Provides
import com.mohiva.play.silhouette.api.{Environment => SocialEnvironment}
import net.codingwell.scalaguice.ScalaModule
import org.joda.time.DateTimeZone
import play.api.libs.ws.WSClient
import play.api.{Configuration, Environment, Mode}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Module with all service dependencies
  */
class UserModule extends ScalaModule {

  override def configure(): Unit = {
    DateTimeZone.setDefault(DateTimeZone.UTC)

    bind(classOf[UserService]).to(classOf[SimpleUserService])
    bind(classOf[UserRepository]).to(classOf[MongoUserRepository])
  }

  @Provides
  @Named("user")
  @Singleton
  def userMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("user", mongoApi, ec)
  }

  @Provides
  @Singleton
  def subscriptionManager(ws: WSClient, conf: Configuration, env: Environment, ec: ExecutionContext): SubscriptionManager = {
    if (env.mode == Mode.Test) {
      TestSubscriptionManager
    } else {
      val apiKey = conf.getString("email.mailgun.api.key").get
      MailgunSubscriptionManager(apiKey, ws, ec)
    }
  }



}
