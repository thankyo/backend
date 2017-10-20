package com.clemble.loveit.auth.service.repository.mongo

import java.util.UUID
import javax.inject.{Inject, Named, Singleton}

import com.clemble.loveit.auth.model.AuthToken
import com.clemble.loveit.auth.service.repository.AuthTokenRepository
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.mongo.MongoSafeUtils
import play.api.libs.json._
import reactivemongo.play.json._
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONElement, BSONInteger}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

/**
 * Give access to the [[AuthToken]] object.
 */
@Singleton
case class MongoAuthTokenRepository @Inject()(@Named("authToken") collection: JSONCollection)(implicit ec: ExecutionContext) extends AuthTokenRepository {

  MongoAuthTokenRepository.ensureMeta(collection)

  /**
   * Finds a token by its ID.
   *
   * @param token The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(token: UUID): Future[Option[AuthToken]] = {
    val selector = Json.obj("token" -> token)
    collection.find(selector).one[AuthToken]
  }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken] = {
    MongoSafeUtils.safe(token, collection.insert[AuthToken](token))
  }

  /**
   * Removes the token for the given ID.
   *
   * @param token The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(token: UUID): Future[Boolean] = {
    val selector = Json.obj("token" -> token)
    MongoSafeUtils.safeSingleUpdate(collection.remove(selector))
  }

  override def removeByUser(user: UserID) = {
    val selector = Json.obj("user" -> user)
    MongoSafeUtils.safeSingleUpdate(collection.remove(selector))
  }

}

/**
 * The companion object.
 */
object MongoAuthTokenRepository {

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
