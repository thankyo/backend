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

  private def doCreate(collectionName: String, mongoApi: ReactiveMongoApi, dropExisting: Boolean)(implicit ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      flatMap(db => {
        db.collectionNames.flatMap(names => {
          val jsonColl = db.collection[JSONCollection](collectionName, FailoverStrategy.default)
          if (dropExisting) {
            jsonColl.drop(false).flatMap(_ => jsonColl.create()).map(_ => jsonColl)
          } else if (names.contains(collectionName)) {
            Future.successful(jsonColl)
          } else {
            jsonColl.create().map(_ => jsonColl)
          }
        })
      })
    Await.result(fCollection, 1.minute)
  }

  def create(collectionName: String, mongoApi: ReactiveMongoApi, ec: ExecutionContext, env: Environment): JSONCollection = {
    doCreate(collectionName, mongoApi, env.mode == Mode.Test)(ec)
  }

}
