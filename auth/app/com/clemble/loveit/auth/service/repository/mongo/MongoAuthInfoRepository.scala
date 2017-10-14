package com.clemble.loveit.auth.service.repository.mongo

import javax.inject.{Inject, Named, Singleton}

import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.api.{AuthInfo, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.{OAuth1Info, OAuth2Info, OpenIDInfo}
import reactivemongo.api.indexes.{Index, IndexType}
import play.api.libs.json._
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

case class MongoAuthInfo(
                          loginInfo: LoginInfo,
                          authInfo: AuthInfo
                        )

object MongoAuthInfo {

  implicit val passwordFormat: OFormat[PasswordInfo] = Json.format[PasswordInfo]
  implicit val oauth2InfoFormat: OFormat[OAuth2Info] = Json.format[OAuth2Info]
  implicit val oauth1InfoFormat: OFormat[OAuth1Info] = Json.format[OAuth1Info]
  implicit val openIDInfoFormat: OFormat[OpenIDInfo] = Json.format[OpenIDInfo]

  implicit val authInfoFormat: OFormat[AuthInfo] = new OFormat[AuthInfo] {
    val PASSWORD = JsString("password")
    val OAUTH1 = JsString("oauth1")
    val OAUTH2 = JsString("oauth2")
    val OPEN_ID = JsString("openID")

    override def writes(o: AuthInfo) = o match {
      case password: PasswordInfo => passwordFormat.writes(password) + ("type" -> PASSWORD)
      case oauth1: OAuth1Info => oauth1InfoFormat.writes(oauth1) + ("type" -> OAUTH1)
      case oauth2: OAuth2Info => oauth2InfoFormat.writes(oauth2) + ("type" -> OAUTH2)
      case openID: OpenIDInfo => openIDInfoFormat.writes(openID) + ("type" -> OPEN_ID)
    }

    override def reads(json: JsValue) = (json \ "type").
      toOption.
      collect({
        case PASSWORD => passwordFormat.reads (json)
        case OAUTH1 => oauth1InfoFormat.reads (json)
        case OAUTH2 => oauth2InfoFormat.reads (json)
        case OPEN_ID => openIDInfoFormat.reads (json)
      }).getOrElse(JsError(s"Can't parse ${json} as AuthInfo"))
  }

  implicit val jsonFormat: OFormat[MongoAuthInfo] = Json.format[MongoAuthInfo]

}

@Singleton
case class MongoAuthInfoRepository @Inject()(@Named("authInfo") collection: JSONCollection)(implicit ec: ExecutionContext) extends AuthInfoRepository {

  MongoAuthInfoRepository.ensureMeta(collection)

  override def find[T <: AuthInfo](loginInfo: LoginInfo)(implicit tag: ClassTag[T]): Future[Option[T]] = {
    val selector = Json.obj("loginInfo" -> loginInfo)
    collection.find(selector).
      one[MongoAuthInfo].
      map(_.map(_.authInfo.asInstanceOf[T]))
  }

  override def add[T <: AuthInfo](loginInfo: LoginInfo, authInfo: T): Future[T] = {
    val storeObj = MongoAuthInfo(loginInfo, authInfo)
    MongoSafeUtils.
      safeSingleUpdate(collection.insert[MongoAuthInfo](storeObj)).
      map(saved => if (saved) authInfo else throw new IllegalArgumentException("Failed to save authentication"))
  }

  override def update[T <: AuthInfo](loginInfo: LoginInfo, authInfo: T): Future[T] = {
    val selector = Json.obj("loginInfo" -> loginInfo)
    val update = Json.obj("$set" -> Json.obj("authInfo" -> MongoAuthInfo.authInfoFormat.writes(authInfo)))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update)).
      map(updated => if (updated) authInfo else throw new IllegalArgumentException("Failed to save authentication"))
  }

  override def save[T <: AuthInfo](loginInfo: LoginInfo, authInfo: T): Future[T] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  override def remove[T <: AuthInfo](loginInfo: LoginInfo)(implicit tag: ClassTag[T]): Future[Unit] = {
    val selector = Json.obj("loginInfo" -> loginInfo)
    MongoSafeUtils.safeSingleUpdate(collection.remove(selector)).
      map(_ => {})
  }
}

object MongoAuthInfoRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
    ensureIndexes(collection)
  }

  private def ensureIndexes(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        key = Seq("loginInfo.providerID" -> IndexType.Ascending, "loginInfo.providerKey" -> IndexType.Ascending),
        name = Some("login_info"),
        unique = true
      )
    )
  }

}