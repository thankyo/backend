package com.clemble.loveit.thank

import com.clemble.loveit.thank.service.repository.{ROVerificationRepository, ResourceRepository, ThankRepository, UserResourceRepository}
import com.clemble.loveit.thank.service.repository.mongo.{MongoROVerificationRepository, MongoResourceRepository, MongoThankRepository, MongoUserResourceRepository}
import com.clemble.loveit.thank.service._
import com.google.inject.Provides
import javax.inject.{Named, Singleton}

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.mongo.JSONCollectionFactory
import net.codingwell.scalaguice.ScalaModule
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class ThankModule extends ScalaModule {

  override def configure(): Unit = {
    bind(classOf[ThankService]).to(classOf[SimpleThankService])
    bind(classOf[ThankRepository]).to(classOf[MongoThankRepository])

    bind(classOf[ResourceRepository]).to(classOf[MongoResourceRepository]).asEagerSingleton()
    bind(classOf[ResourceOwnershipService]).to(classOf[SimpleResourceOwnershipService])

    bind(classOf[UserResourceRepository]).to(classOf[MongoUserResourceRepository])

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
    JSONCollectionFactory.create("thank", mongoApi, ec)
  }

}
