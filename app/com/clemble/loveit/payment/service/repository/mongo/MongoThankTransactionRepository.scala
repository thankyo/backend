package com.clemble.loveit.payment.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.common.mongo.MongoUserAwareRepository
import com.clemble.loveit.payment.model.ThankTransaction
import com.clemble.loveit.payment.service.repository.ThankTransactionRepository
import javax.inject.{Named, Inject, Singleton}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoThankTransactionRepository @Inject()(
                                                      @Named("thankTransactions") collection: JSONCollection,
                                                      implicit val m: Materializer,
                                                      implicit val ec: ExecutionContext)
  extends ThankTransactionRepository
    with MongoUserAwareRepository[ThankTransaction] {

  override implicit val format = ThankTransaction.jsonFormat

  override def save(payment: ThankTransaction): Future[ThankTransaction] = {
    collection.insert(payment).filter(_.ok).map(_ => payment)
  }

}
