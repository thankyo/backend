package com.clemble.thank

import com.clemble.thank.service.repository.{MongoThankRepository, MongoUserRepository, ThankRepository, UserRepository}
import com.clemble.thank.service.{SimpleThankService, SimpleUserService, ThankService, UserService}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Module with all service dependencies
  */
class ServiceModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[UserService]).to(classOf[SimpleUserService])
    bind(classOf[UserRepository]).to(classOf[MongoUserRepository])

    bind(classOf[ThankService]).to(classOf[SimpleThankService])
    bind(classOf[ThankRepository]).to(classOf[MongoThankRepository])
  }

  @Provides
  @Named("user")
  def userMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection]("user", FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }

  @Provides
  @Named("thank")
  def thankMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection]("thank", FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }

}
