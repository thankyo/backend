package com.clemble.loveit.thank

import com.clemble.loveit.thank.service.repository._
import com.clemble.loveit.thank.service.repository.mongo._
import com.clemble.loveit.thank.service._
import com.google.inject.Provides
import javax.inject.{Inject, Named, Singleton}

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.mongo.JSONCollectionFactory
import com.mohiva.play.silhouette.api.crypto.Crypter
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import net.codingwell.scalaguice.ScalaModule
import play.api.{ConfigLoader, Configuration, Environment}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext

class ThankModule @Inject()(env: Environment, conf: Configuration) extends ScalaModule {

  override def configure(): Unit = {
    bind(classOf[ThankService]).to(classOf[SimpleThankService])
    bind(classOf[ThankRepository]).to(classOf[MongoThankRepository])

    bind(classOf[ResourceRepository]).to(classOf[MongoResourceRepository]).asEagerSingleton()
    bind(classOf[ROService]).to(classOf[SimpleResourceOwnershipService])

    bind(classOf[UserSupportedProjectsService]).to(classOf[SimpleUserSupportedProjectsService])
    bind(classOf[UserSupportedProjectsRepo]).to(classOf[MongoUserSupportedProjectsRepo])

    bind(classOf[UserResourceRepository]).to(classOf[MongoUserResourceRepository])
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
  @Named("thank")
  def thankMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("thank", mongoApi, ec, env)
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
