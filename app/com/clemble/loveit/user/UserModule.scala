package com.clemble.loveit.user

import com.clemble.loveit.user.service._
import com.clemble.loveit.user.service.repository._
import com.clemble.loveit.user.service.repository.mongo.MongoUserRepository
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}
import org.joda.time.DateTimeZone
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Module with all service dependencies
  */
class UserModule extends AbstractModule {

  override def configure(): Unit = {
    DateTimeZone.setDefault(DateTimeZone.UTC)

    bind(classOf[UserService]).to(classOf[SimpleUserService])
    bind(classOf[UserRepository]).to(classOf[MongoUserRepository])
  }

  @Provides
  @Named("user")
  @Singleton
  def userMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection]("user", FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }



}
