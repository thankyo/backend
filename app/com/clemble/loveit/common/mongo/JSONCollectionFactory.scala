package com.clemble.loveit.common.mongo

import play.api.Environment
import play.api.Mode.Mode
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Configuration for JSON collection
  */
object JSONCollectionFactory {

  private def doCreate(collectionName: String, mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection](collectionName, FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }

  def create(collectionName: String, mongoApi: ReactiveMongoApi, ec: ExecutionContext, env: Environment): JSONCollection = {
    val collection = doCreate(collectionName, mongoApi, ec)
    collection
  }

}
