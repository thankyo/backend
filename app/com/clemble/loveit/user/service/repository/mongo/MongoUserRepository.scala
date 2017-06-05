package com.clemble.loveit.user.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.user.model._
import com.clemble.loveit.common.model.{Amount, Resource, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.payment.model.{BankDetails, UserPayment}
import com.clemble.loveit.user.service.repository.UserRepository
import javax.inject.{Inject, Named, Singleton}

import akka.stream.scaladsl.Sink
import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json._
import reactivemongo.play.json._
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

  MongoUserRepository.ensureMeta(collection)

  override def save(user: User): Future[User] = {
    val userJson = Json.toJson[User](user).as[JsObject] + ("_id" -> JsString(user.id))
    val fInsert = collection.insert(userJson)
    MongoSafeUtils.safe(user, fInsert)
  }

  override def update(user: User): Future[User] = {
    val selector = Json.obj("_id" -> JsString(user.id))
    val update = Json.toJson(user).as[JsObject]
    val fUpdate = collection.update(selector, update)
    MongoSafeUtils.safe(user, fUpdate)
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

  override def remove(users: Seq[UserID]): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$in" -> JsArray(users.map(JsString))))
    val fRemove = collection.remove(query).map(_.ok)
    MongoSafeUtils.safe(fRemove)
  }

}

object MongoUserRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    ensureIndexes(collection)
    ensureUpToDate(collection)
  }

  private def ensureIndexes(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
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

  private def ensureUpToDate(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    addTotalField(collection)
    addBioField(collection)
    addOwnRequests(collection)
    addMonthlyLimit(collection)
    changeOwner(collection)
    removeSocialResources(collection)
  }

  private def addTotalField(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    val selector = Json.obj("total" -> Json.obj("$exists" -> false))
    val update = (user: JsObject) => {
      val id = (user \ "_id").as[String]
      val balance = (user \ "balance").as[Amount]
      collection.update(Json.obj("_id" -> id), Json.obj("$set" -> Json.obj("total" -> balance)))
    }
    MongoSafeUtils.ensureUpdate(collection, selector, update)
  }

  private def addBioField(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    val selector = Json.obj("bio" -> Json.obj("$exists" -> false))
    val update = Json.obj("$set" -> Json.obj("bio" -> User.DEFAULT_BIO))
    val fUpdate = collection.update(selector, update, upsert = false, multi = true)
    fUpdate.foreach(res => if (!res.ok) System.exit(2));
  }

  private def addOwnRequests(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    val selector = Json.obj("ownRequests" -> Json.obj("$exists" -> false))
    val update = Json.obj("$set" -> Json.obj("ownRequests" -> JsArray()))
    val fUpdate = collection.update(selector, update, upsert = false, multi = true)
    fUpdate.foreach(res => if (!res.ok) System.exit(2));
  }

  private def addMonthlyLimit(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    val selector = Json.obj("monthlyLimit" -> Json.obj("$exists" -> false))
    val update = Json.obj("$set" -> Json.obj("monthlyLimit" -> UserPayment.DEFAULT_LIMIT))
    val fUpdate = collection.update(selector, update, upsert = false, multi = true)
    fUpdate.foreach(res => if (!res.ok) System.exit(2));
  }

  private def changeOwner(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    val selector = Json.obj("owns.ownershipType" -> Json.obj("$exists" -> true))
    val projection = Json.obj("owns" -> 1)
    val source = collection.find(selector, projection).cursor[JsObject](ReadPreference.primary).documentSource()
    source.runWith(Sink.foreach((json) => {
      val id = (json \ "_id").as[String]
      val owns = (json \ "owns").as[JsArray]
      val resources = owns.value.map(ownership => (ownership \ "resource").as[JsObject])
      collection.update(Json.obj("_id" -> id), Json.obj("$set" -> Json.obj("owns" -> resources)))
    }))
  }

  private def removeSocialResources(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    val selector = Json.obj("owns.type" -> "social")
    val projection = Json.obj("owns" -> Json.obj("$pull" -> Json.obj("type" -> "social")))
    collection.update(selector, projection, multi = true)
  }

}
