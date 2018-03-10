package com.clemble.loveit.thank

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorSystem, Scheduler}
import com.clemble.loveit.common.mongo.JSONCollectionFactory
import com.clemble.loveit.thank.service._
import com.clemble.loveit.thank.service.repository._
import com.clemble.loveit.thank.service.repository.mongo._
import com.clemble.loveit.user.service.UserService
import com.google.inject.Provides
import com.mohiva.play.silhouette.api.crypto.Crypter
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.WSClient
import play.api.{Configuration, Environment, Mode}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext

class ThankModule @Inject()(env: Environment, conf: Configuration) extends ScalaModule {

  override def configure(): Unit = {
    bind(classOf[PostService]).to(classOf[SimplePostService])
    bind(classOf[PostRepository]).to(classOf[MongoPostRepository])

    bind(classOf[ProjectLookupService]).to(classOf[SimpleProjectLookupService])
    bind(classOf[ProjectFeedService]).to(classOf[SimpleProjectFeedService])
    bind(classOf[ProjectOwnershipService]).to(classOf[SimpleProjectOwnershipService])

    bind(classOf[ProjectRepository]).to(classOf[MongoProjectRepository]).asEagerSingleton()
    bind(classOf[ProjectService]).to(classOf[SimpleProjectService]).asEagerSingleton()

    bind(classOf[ProjectSupportTrackRepository]).to(classOf[MongoProjectSupportTrackRepository]).asEagerSingleton()
    bind(classOf[ProjectSupportTrackService]).to(classOf[SimpleProjectSupportTrackService]).asEagerSingleton()

    bind(classOf[UserStatService]).to(classOf[SimpleUserStatService]).asEagerSingleton()
    bind(classOf[UserStatRepo]).to(classOf[MongoUserStatRepo])
  }

  @Provides
  @Singleton
  def projectOwnershipVerificationService(ownershipSvc: ProjectOwnershipService, ec: ExecutionContext): ProjectOwnershipVerificationService = {
    if (env.mode == Mode.Test || env.mode == Mode.Dev) {
      TestProjectOwnershipVerificationService
    } else {
      SimpleProjectOwnershipVerificationService(ownershipSvc, ec)
    }
  }

  @Provides
  @Singleton
  def postEnrichService(wsClient: WSClient, ec: ExecutionContext): PostEnrichService = {
    if (env.mode == Mode.Test) {
      TestPostEnrichService
    } else {
      SimplePostEnrichService(wsClient, ec)
    }
  }

  @Provides
  @Singleton
  @Named("post")
  def postMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("post", mongoApi, ec, env)
  }

  @Provides
  @Singleton
  @Named("userResource")
  def userResourceCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("userResource", mongoApi, ec, env)
  }

  @Provides
  @Singleton
  @Named("projects")
  def projectsCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("projects", mongoApi, ec, env)
  }

  @Provides
  @Singleton
  @Named("userSupported")
  def userSupportedCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("userSupported", mongoApi, ec, env)
  }

  @Provides
  @Singleton
  @Named("stat")
  def statMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("stat", mongoApi, ec, env)
  }

  @Provides
  @Singleton
  @Named("rovCrypter")
  def rovCrypter(): Crypter = {
    val key = conf.get[String]("thank.crypter.key")
    val config = JcaCrypterSettings(key)
    new JcaCrypter(config)
  }

  @Provides
  @Singleton
  def projectEnrichService(wsClient: WSClient, userService: UserService, actorSystem: ActorSystem)(implicit ec: ExecutionContext): ProjectEnrichService = {
    val host = conf.get[String]("thank.resource.analyzer.host")
    val port = conf.get[Int]("thank.resource.analyzer.port")
    SimpleProjectEnrichService(s"http://${host}:${port}/lookup/v1/", wsClient, userService, actorSystem.scheduler)
  }

}
