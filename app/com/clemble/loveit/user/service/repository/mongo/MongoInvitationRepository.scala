package com.clemble.loveit.user.service.repository.mongo

import javax.inject.{Inject, Named}

import akka.stream.Materializer
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.user.model.Invitation
import com.clemble.loveit.user.service.repository.InvitationRepository
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

class MongoInvitationRepository @Inject()(
                                           @Named("invitation") collection: JSONCollection,
                                           implicit val m: Materializer,
                                           implicit val ec: ExecutionContext
                                         ) extends InvitationRepository {

  override def save(inv: Invitation): Future[Invitation] = {
    val invJson = Json.toJson[Invitation](inv).as[JsObject]
    val fInsert = collection.insert(invJson)
    MongoSafeUtils.safe(inv, fInsert)
  }

}
