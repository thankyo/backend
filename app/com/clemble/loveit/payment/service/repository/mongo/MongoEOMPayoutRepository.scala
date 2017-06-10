package com.clemble.loveit.payment.service.repository.mongo

import javax.inject.{Inject, Named}

import com.clemble.loveit.payment.model.EOMPayout
import com.clemble.loveit.payment.service.repository.EOMPayoutRepository
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

@Singleton()
case class MongoEOMPayoutRepository @Inject()(@Named("eomPayout") collection: JSONCollection) extends EOMPayoutRepository {

  override def save(payout: EOMPayout): Future[Boolean] = Future.successful(true)

}
