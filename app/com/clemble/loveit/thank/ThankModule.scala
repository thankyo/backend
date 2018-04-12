package com.clemble.loveit.thank

import javax.inject.{Inject, Named, Singleton}
import akka.actor.{ActorSystem, Scheduler}
import com.clemble.loveit.common.model.WordPress
import com.clemble.loveit.common.mongo.JSONCollectionFactory
import com.clemble.loveit.common.service.UserService
import com.clemble.loveit.thank.service._
import com.clemble.loveit.thank.service.repository._
import com.clemble.loveit.thank.service.repository.mongo._
import com.google.inject.Provides
import com.mohiva.play.silhouette.api.crypto.Crypter
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.WSClient
import play.api.{Configuration, Environment, Mode}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext
import scala.io.Source

class ThankModule @Inject()(env: Environment, conf: Configuration) extends ScalaModule {

  override def configure(): Unit = {
    bind(classOf[PostService]).to(classOf[SimplePostService])
    bind(classOf[PostRepository]).to(classOf[MongoPostRepository])

    bind(classOf[ProjectLookupService]).to(classOf[SimpleProjectLookupService])
    bind(classOf[ProjectFeedService]).to(classOf[SimpleProjectFeedService])
    bind(classOf[ProjectOwnershipService]).to(classOf[SimpleProjectOwnershipService])

    bind(classOf[TumblrIntegrationService]).to(classOf[SimpleTumblrIntegrationService]).asEagerSingleton()

    bind(classOf[ProjectRepository]).to(classOf[MongoProjectRepository]).asEagerSingleton()
    bind(classOf[ProjectService]).to(classOf[SimpleProjectService]).asEagerSingleton()

    bind(classOf[ProjectEnrichService]).to(classOf[SimpleProjectEnrichService]).asEagerSingleton()

    bind(classOf[ProjectSupportTrackRepository]).to(classOf[MongoProjectSupportTrackRepository]).asEagerSingleton()
    bind(classOf[ProjectSupportTrackService]).to(classOf[SimpleProjectSupportTrackService]).asEagerSingleton()

    bind(classOf[UserStatService]).to(classOf[SimpleUserStatService]).asEagerSingleton()
    bind(classOf[UserStatRepo]).to(classOf[MongoUserStatRepo])

    if (env.mode == Mode.Test) {
      bind(classOf[PostEnrichService]).toInstance(TestPostEnrichService)
    } else {
      bind(classOf[PostEnrichService]).to(classOf[SimplePostEnrichService])
    }

    if (env.mode == Mode.Test || env.mode == Mode.Dev) {
      bind(classOf[ProjectOwnershipVerificationService]).toInstance(TestProjectOwnershipVerificationService)
    } else {
      bind(classOf[ProjectOwnershipVerificationService]).to(classOf[SimpleProjectOwnershipVerificationService])
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
  def projectWebStackAnalysis(wsClient: WSClient, userService: UserService, actorSystem: ActorSystem)(implicit ec: ExecutionContext): ProjectWebStackAnalysis = {
    val analyzed = Source.fromResource("thank/analysis/wordPress.txt").getLines().map(url => url -> WordPress).toMap
    StaticWebStackAnalyzer(analyzed)
  }

}
