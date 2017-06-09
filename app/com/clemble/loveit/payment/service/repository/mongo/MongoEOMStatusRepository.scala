package com.clemble.loveit.payment.service.repository.mongo

import java.time.YearMonth
import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.payment.model.EOMStatus
import com.clemble.loveit.payment.service.repository.EOMStatusRepository
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoEOMStatusRepository @Inject()(@Named("eomStatus") collection: JSONCollection, implicit val ec: ExecutionContext, implicit val m: Materializer) extends EOMStatusRepository {

  override def get(yom: YearMonth): Future[Option[YearMonth]] = ???

  override def save(status: EOMStatus): Future[EOMStatus] = ???

}
