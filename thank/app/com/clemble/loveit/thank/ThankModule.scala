package com.clemble.loveit.thank

import javax.inject.{Inject, Named, Singleton}

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.mongo.JSONCollectionFactory
import com.clemble.loveit.thank.service._
import com.clemble.loveit.thank.service.repository._
import com.clemble.loveit.thank.service.repository.elastic.ElasticPostRepository
import com.clemble.loveit.thank.service.repository.mongo._
import com.google.inject.Provides
import com.mohiva.play.silhouette.api.crypto.Crypter
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext

class ThankModule @Inject()(env: Environment, conf: Configuration) extends ScalaModule {

  override def configure(): Unit = {
    bind(classOf[PostService]).to(classOf[SimplePostService])
    bind(classOf[PostRepository]).to(classOf[MongoPostRepository])

    bind(classOf[RORepository]).to(classOf[MongoRORepository]).asEagerSingleton()
    bind(classOf[ROService]).to(classOf[SimpleResourceOwnershipService])
    bind(classOf[UserResourceService]).to(classOf[SimpleUserResourceService]).asEagerSingleton()

    bind(classOf[SupportedProjectRepository]).to(classOf[MongoSupportedProjectRepository]).asEagerSingleton()
    bind(classOf[SupportTrackRepository]).to(classOf[MongoSupportTrackRepository]).asEagerSingleton()
    bind(classOf[SupportedProjectService]).to(classOf[SimpleSupportedProjectService]).asEagerSingleton()

    bind(classOf[UserResourceRepository]).to(classOf[MongoUserResourceRepository])
    bind(classOf[UserStatService]).to(classOf[SimpleUserStatService]).asEagerSingleton()
    bind(classOf[UserStatRepo]).to(classOf[MongoUserStatRepo])

    ownershipVerification()
  }

  def ownershipVerification(): Unit = {
    bind(classOf[ROVerificationGenerator]).to(classOf[CryptROVerificationGenerator])
    bind(classOf[ROVerificationRepository]).to(classOf[MongoROVerificationRepository])
    bind(classOf[MetaTagReader]).to(classOf[WSMetaTagReader])
    bind(classOf[ROVerificationService]).to(classOf[SimpleROVerificationService])
  }

  @Provides
  @Singleton
  def resourceVerificationService(httpVerification: HttpROVerificationConfirmationService): ROVerificationConfirmationService[Resource] = {
    ROVerificationConfirmationFacade(httpVerification)
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

}
