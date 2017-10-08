package com.clemble.loveit.common.mongo

import com.mohiva.play.silhouette.api.Logger
import play.api.{Environment, Mode}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Configuration for JSON collection
  */
object JSONCollectionFactory extends Logger {

  private def doCreate(collectionName: String, mongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection](collectionName, FailoverStrategy.default))
    fCollection.failed.foreach({ case err =>
      logger.error(s"Failed to create ${collectionName}", err);
      Thread.sleep(60000)
      System.exit(1)
    })
    Await.result(fCollection, 1 minute)
  }

  def create(collectionName: String, mongoApi: ReactiveMongoApi, ec: ExecutionContext, env: Environment): JSONCollection = {
    val collection = doCreate(collectionName, mongoApi)(ec)
    if (env.mode == Mode.Test) Await.result(collection.drop(false)(ec), 1 minute)
    collection
  }

}
