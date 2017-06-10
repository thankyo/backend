package com.clemble.loveit.common.mongo

import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Configuration for JSON collection
  */
object JSONCollectionFactory {

  def create(collectionName: String, mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection](collectionName, FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }

}
