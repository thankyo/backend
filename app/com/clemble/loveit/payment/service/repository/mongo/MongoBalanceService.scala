package com.clemble.loveit.payment.service.repository.mongo

import javax.inject.{Inject, Named, Singleton}

import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.payment.service.repository.BalanceService
import play.api.libs.json.Json
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoBalanceService @Inject()(@Named("user") collection: JSONCollection, implicit val ec: ExecutionContext) extends BalanceService {

  override def updateBalance(user: UserID, update: Amount): Future[Boolean] = {
    val query = Json.obj("_id" -> user)
    val change = Json.obj("$inc" -> Json.obj("balance" -> update))
    MongoSafeUtils.safeSingleUpdate(collection.update(query, change))
  }

}
