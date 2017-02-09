package com.clemble.thank.service

import com.clemble.thank.service.repository.{MongoUserRepository, UserRepository}
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
  }

  @Provides
  @Named("user")
  def userMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection]("user", FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }


}
