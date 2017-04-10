package com.clemble.loveit.thank

import com.clemble.loveit.thank.service.repository.ThankRepository
import com.clemble.loveit.thank.service.repository.mongo.MongoThankRepository
import com.clemble.loveit.thank.service._
import com.google.inject.{AbstractModule, Provides, Singleton}
import com.google.inject.name.Named
import play.api.Mode
import play.api.{Configuration, Environment}
import play.api.libs.ws.WSClient
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class ThankModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[ThankService]).to(classOf[SimpleThankService])
    bind(classOf[ThankRepository]).to(classOf[MongoThankRepository])

    bind(classOf[ResourceOwnershipService]).to(classOf[SimpleResourceOwnershipService])
  }

  @Provides
  @Singleton
  def ga(ws: WSClient, ec: ExecutionContext, env: Environment): AnalyticsService = {
    if (env.mode == Mode.Test)
      StubAnalyticsService
    else
      GoogleAnalyticsService("UA-96949345-1", ws, ec)
  }

  @Provides
  @Singleton
  @Named("thank")
  def thankMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection]("thank", FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }

}
