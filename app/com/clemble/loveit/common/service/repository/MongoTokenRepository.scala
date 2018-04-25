package com.clemble.loveit.common.service.repository

import java.util.UUID

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.common.model.{TokenAware, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.common.service.TokenRepository
import javax.inject.{Inject, Named, Singleton}
import play.api.libs.json._
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONElement, BSONInteger}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

/**
  * Give access to the [[ResetPasswordToken]] object.
  */
@Singleton
case class MongoTokenRepository[T <: TokenAware] @Inject()(
  @Named("resetToken") collection: JSONCollection)
  (implicit val ec: ExecutionContext, implicit val format: OFormat[T])
  extends TokenRepository[T] {

  MongoTokenRepository.ensureMeta(collection)

  def findByToken(token: UUID): Future[Option[T]] = {
    val selector = Json.obj("token" -> token)
    collection.find(selector).one[T]
  }

  def save(token: T): Future[T] = {
    MongoSafeUtils.safe(token, collection.insert[T](token))
  }

  def removeByToken(token: UUID): Future[Boolean] = {
    val selector = Json.obj("token" -> token)
    MongoSafeUtils.safeSingleUpdate(collection.remove(selector))
  }

  override def removeByUser(user: UserID): Future[Boolean] = {
    val selector = Json.obj("user" -> user)
    collection.remove(selector).map(_.ok)
  }

}

/**
  * The companion object.
  */
object MongoTokenRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
    ensureIndexes(collection)
  }

  private def ensureIndexes(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        key = Seq("created" -> IndexType.Ascending),
        name = Some("created_expire"),
        options = BSONDocument(BSONElement("expireAfterSeconds", BSONInteger(3600)))
      )
    )
  }
}
