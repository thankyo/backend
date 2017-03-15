package com.clemble.thank.service.repository.mongo

import akka.stream.Materializer
import com.clemble.thank.model.{Payment, UserID}
import com.clemble.thank.service.repository.PaymentRepository
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import play.api.libs.json.Json
import reactivemongo.api.{ReadPreference, SortOrder}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import akka.stream.scaladsl.Source
import reactivemongo.akkastream.cursorProducer
import reactivemongo.api.indexes.{Index, IndexType}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoPaymentRepository @Inject()(
                                             @Named("payment") collection: JSONCollection,
                                             implicit val m: Materializer,
                                             implicit val ec: ExecutionContext) extends PaymentRepository {

  MongoSafeUtils.ensureIndexes(
    collection,
    Index(
      key = Seq("user" -> IndexType.Ascending, "createdDate" -> IndexType.Ascending),
      name = Some("user_created_date_asc")
    ),
    Index(
      key = Seq("user" -> IndexType.Ascending),
      name = Some("user_asc")
    )
  )

  override def findByUser(user: UserID): Source[Payment, _] = {
    collection.find(Json.obj("user" -> user)).sort(Json.obj("createdDate" -> 1)).cursor[Payment](ReadPreference.nearest).documentSource()
  }

  override def save(payment: Payment): Future[Payment] = {
    collection.insert(payment).filter(_.ok).map(_ => payment)
  }

}
