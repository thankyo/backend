package com.clemble.loveit.thank

import com.clemble.loveit.thank.service.repository.ThankRepository
import com.clemble.loveit.thank.service.repository.mongo.MongoThankRepository
import com.clemble.loveit.thank.service.{SimpleThankService, ThankService}
import com.google.inject.{AbstractModule, Provides}
import com.google.inject.name.Named
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._

import scala.concurrent.{Await, ExecutionContext, Future}

class ThankModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[ThankService]).to(classOf[SimpleThankService])
    bind(classOf[ThankRepository]).to(classOf[MongoThankRepository])
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
