package com.clemble.loveit.thank

import javax.inject.{Inject, Named, Singleton}
import akka.actor.{ActorSystem, Scheduler}
import com.clemble.loveit.common.model.WordPress
import com.clemble.loveit.common.mongo.JSONCollectionFactory
import com.clemble.loveit.common.service.repository.MongoTokenRepository
import com.clemble.loveit.common.service._
import com.clemble.loveit.thank.service._
import com.clemble.loveit.thank.service.repository._
import com.clemble.loveit.thank.service.repository.mongo._
import com.google.inject.Provides
import com.google.inject.name.Names
import com.mohiva.play.silhouette.api.crypto.Crypter
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.WSClient
import play.api.{Configuration, Environment, Mode}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext
import scala.io.Source

class ThankModule @Inject()(env: Environment, conf: Configuration) extends ScalaModule {

  override def configure(): Unit = {
    if (env.mode == Mode.Test) {
      bind(classOf[URLValidator]).toInstance(TestURLValidator)
    } else {
      bind(classOf[URLValidator]).to(classOf[SimpleURLValidator])
    }

    bind(classOf[PostService]).to(classOf[SimplePostService])
    bind(classOf[PostRepository]).to(classOf[MongoPostRepository])

    bind(classOf[ProjectLookupService]).to(classOf[SimpleProjectLookupService])
    bind(classOf[ProjectFeedService]).to(classOf[SimpleProjectFeedService])

    bind(classOf[TumblrIntegrationService]).to(classOf[SimpleTumblrIntegrationService]).asEagerSingleton()
    bind(classOf[UserProjectsRepository]).to(classOf[MongoUserProjectsRepository]).asEagerSingleton()
    bind(classOf[ProjectRepository]).to(classOf[MongoUserProjectsRepository]).asEagerSingleton()
    bind(classOf[ProjectService]).to(classOf[SimpleProjectService]).asEagerSingleton()

    bind[UserProjectsService].to[SimpleUserProjectsService].asEagerSingleton()

    if (env.mode == Mode.Test) {
      bind(classOf[WHOISService]).toInstance(TestWHOISService)
    } else {
      bind(classOf[String]).annotatedWith(Names.named("thank.whois.key")).toInstance(conf.get[String]("thank.whois.key"))
      bind(classOf[WHOISService]).to(classOf[SimpleWHOISService])
    }
    bind(classOf[ProjectOwnershipByEmailService]).to(classOf[SimpleProjectOwnershipByEmailService])

    bind(classOf[ProjectSupportTrackRepository]).to(classOf[MongoProjectSupportTrackRepository]).asEagerSingleton()
    bind(classOf[ProjectSupportTrackService]).to(classOf[SimpleProjectSupportTrackService]).asEagerSingleton()

    bind(classOf[TumblrAPI]).to(classOf[SimpleTumblrAPI])

    bind(classOf[UserStatService]).to(classOf[SimpleUserStatService]).asEagerSingleton()
    bind(classOf[UserStatRepo]).to(classOf[MongoUserStatRepo])

    if (env.mode == Mode.Test) {
      bind(classOf[ProjectEnrichService]).toInstance(TestProjectEnrichService)
    } else {
      bind(classOf[ProjectEnrichService]).to(classOf[SimpleProjectEnrichService]).asEagerSingleton()
    }

    if (env.mode == Mode.Test) {
      bind(classOf[PostEnrichService]).toInstance(TestPostEnrichService)
    } else {
      bind(classOf[PostEnrichService]).to(classOf[SimplePostEnrichService])
    }
  }

  @Provides
  @Singleton
  def projectVerificationByEmailRepo(factory: JSONCollectionFactory)(implicit ec: ExecutionContext): TokenRepository[ProjectOwnershipByEmailToken] = {
    MongoTokenRepository[ProjectOwnershipByEmailToken](factory.create("prj_email_verification_token"))
  }

  @Provides
  @Singleton
  @Named("post")
  def postMongoCollection(collectionFactory: JSONCollectionFactory): JSONCollection = {
    collectionFactory.create("post")
  }

  @Provides
  @Singleton
  @Named("userResource")
  def userResourceCollection(collectionFactory: JSONCollectionFactory): JSONCollection = {
    collectionFactory.create("userResource")
  }

  @Provides
  @Singleton
  @Named("userSupported")
  def userSupportedCollection(collectionFactory: JSONCollectionFactory): JSONCollection = {
    collectionFactory.create("userSupported")
  }

  @Provides
  @Singleton
  @Named("userProject")
  def userProjectCollection(collectionFactory: JSONCollectionFactory): JSONCollection = {
    collectionFactory.create("userProject")
  }

  @Provides
  @Singleton
  @Named("stat")
  def statMongoCollection(collectionFactory: JSONCollectionFactory): JSONCollection = {
    collectionFactory.create("stat")
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
