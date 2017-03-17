package com.clemble.thank.service.repository.mongo

import akka.stream.Materializer
import com.clemble.thank.model.{ThankTransaction, UserID}
import com.clemble.thank.service.repository.ThankTransactionRepository
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
case class MongoThankTransactionRepository @Inject()(
                                             @Named("payment") collection: JSONCollection,
                                             implicit val m: Materializer,
                                             implicit val ec: ExecutionContext) extends ThankTransactionRepository {

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

  override def findByUser(user: UserID): Source[ThankTransaction, _] = {
    collection.find(Json.obj("user" -> user)).sort(Json.obj("createdDate" -> 1)).cursor[ThankTransaction](ReadPreference.nearest).documentSource()
  }

  override def save(payment: ThankTransaction): Future[ThankTransaction] = {
    collection.insert(payment).filter(_.ok).map(_ => payment)
  }

}
