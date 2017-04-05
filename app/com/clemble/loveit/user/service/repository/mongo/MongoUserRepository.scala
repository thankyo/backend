package com.clemble.loveit.user.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.user.model._
import com.clemble.loveit.common.error.UserException
import com.clemble.loveit.common.model.{Amount, Resource, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.payment.model.BankDetails
import com.clemble.loveit.user.service.repository.UserRepository
import com.clemble.loveit.thank.model.ResourceOwnership
import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json._
import reactivemongo.play.json._
import reactivemongo.api.Cursor.ContOnError
import reactivemongo.api.ReadPreference
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.akkastream.cursorProducer

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONString}


@Singleton
case class MongoUserRepository @Inject()(
                                          @Named("user") collection: JSONCollection,
                                          implicit val m: Materializer,
                                          implicit val ec: ExecutionContext
                                        ) extends UserRepository {

  MongoUserRepository.ensureIndexes(collection)
  MongoUserRepository.ensureUpToDate(collection)

  override def save(user: User): Future[User] = {
    val userJson = Json.toJson[User](user).as[JsObject] + ("_id" -> JsString(user.id))
    val fInsert = collection.insert(userJson)
    MongoSafeUtils.safe(() => user, fInsert)
  }

  override def update(user: User): Future[User] = {
    val selector = Json.obj("_id" -> JsString(user.id))
    val update = Json.toJson(user).as[JsObject]
    val fUpdate = collection.update(selector, update)
    MongoSafeUtils.safe(() => user, fUpdate)
  }

  override def findById(id: UserID): Future[Option[User]] = {
    val query = Json.obj("_id" -> id)
    val fUser = collection.find(query).one[User]
    MongoSafeUtils.safe(fUser)
  }

  override def retrieve(loginInfo: LoginInfo): Future[Option[UserIdentity]] = {
    val query = Json.obj("profiles.providerID" -> loginInfo.providerID, "profiles.providerKey" -> loginInfo.providerKey)
    val fUser = collection.find(query).one[User].map(_.map(_.toIdentity()))
    MongoSafeUtils.safe(fUser)
  }

  override def setBankDetails(user: UserID, bankDetails: BankDetails): Future[Boolean] = {
    val query = Json.obj("_id" -> user)
    val change = Json.obj("$set" -> Json.obj("bankDetails" -> bankDetails))
    val update = collection.update(query, change).map(res => res.ok && res.n == 1)
    MongoSafeUtils.safe(update)
  }

  override def changeBalance(id: UserID, diff: Amount): Future[Boolean] = {
    val query = if (diff > 0) { // debit query
      Json.obj("_id" -> id)
    } else { // credit query
      Json.obj("_id" -> id, "balance" -> Json.obj("$gte" -> -diff))
    }
    val change = Json.obj("$inc" -> Json.obj("balance" -> diff))
    val update = collection.
      update(query, change).
      map(res => {
        if (res.ok && res.n != 1) {
          throw UserException.notEnoughFunds()
        } else {
          res.ok
        }
      })
    MongoSafeUtils.safe(update)
  }

  override def findRelated(uri: ResourceOwnership): Future[List[User]] = {
    val query = Json.obj("owns.resource.uri" -> Json.obj("$regex" -> s"${uri.resource}.*"))
    doFind(query)
  }

  override def remove(users: Seq[UserID]): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$in" -> JsArray(users.map(JsString))))
    val fRemove = collection.remove(query).map(_.ok)
    MongoSafeUtils.safe(fRemove)
  }

  override def findOwners(uris: List[ResourceOwnership]): Future[List[User]] = {
    val query = Json.obj("owns" -> Json.obj("$in" -> JsArray(uris.map(Json.toJson(_)))))
    doFind(query)
  }

  private def doFind(query: JsObject): Future[List[User]] = {
    val users = collection.
      find(query).
      cursor[User](ReadPreference.nearest).
      collect[List](Int.MaxValue, ContOnError[List[User]]())
    MongoSafeUtils.safe(users)
  }

}

object MongoUserRepository {

  def ensureIndexes(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        key = Seq("profiles.providerID" -> IndexType.Ascending, "profiles.providerKey" -> IndexType.Ascending),
        name = Some("user_profiles")
      ),
      Index(
        key = Seq("owns.resource.uri" -> IndexType.Ascending, "owns.resource.type" -> IndexType.Ascending),
        name = Some("user_owns_unique"),
        unique = true,
        sparse = true
      ),
      Index(
        key = Seq("bankDetails.type" -> IndexType.Ascending, "bankDetails.email" -> IndexType.Ascending),
        name = Some("user_bank_details_unique"),
        unique = true,
        partialFilter = Some(BSONDocument("bankDetails.type" -> BSONString("payPal")))
      )
    )
  }

  def ensureUpToDate(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    addTotalField(collection)
    addBioField(collection)
  }

  def addTotalField(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    val selector = Json.obj("total" -> Json.obj("$exists" -> false))
    val update = (user: JsObject) => {
      val id = (user \ "_id").as[String]
      val balance = (user \ "balance").as[Amount]
      collection.update(Json.obj("_id" -> id), Json.obj("$set" -> Json.obj("total" -> balance)))
    }
    MongoSafeUtils.ensureUpdate(collection, selector, update)
  }

  def addBioField(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    val selector = Json.obj("bio" -> Json.obj("$exists" -> false))
    val update = Json.obj("$set" -> Json.obj("bio" -> User.DEFAULT_BIO))
    val fUpdate = collection.update(selector, update, upsert = false, multi = true)
    fUpdate.foreach(res => if (!res.ok) System.exit(2));
  }

}
