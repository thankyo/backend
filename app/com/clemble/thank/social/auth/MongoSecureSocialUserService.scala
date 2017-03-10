package com.clemble.thank.social.auth

import play.api.libs.json.{JsArray, JsObject, JsString, Json}

import play.api.libs.json.{Format}
import securesocial.core.{BasicProfile, PasswordInfo}
import securesocial.core.providers.MailToken
import securesocial.core.services.{SaveMode, UserService => SecureSocialUserService}
import com.google.inject.Inject
import com.google.inject.name.Named
import play.api.libs.json.{Json}
import reactivemongo.play.json.collection.JSONCollection
import play.modules.reactivemongo.json._


import scala.concurrent.{ExecutionContext, Future}

case class MongoSecureSocialUserService @Inject()(
                                                    @Named("secureSocial") collection: JSONCollection,
                                                    implicit val ec: ExecutionContext,
                                                    implicit val f: Format[BasicProfile],
                                                    implicit val passwordInfoFormat: Format[PasswordInfo]
) extends SecureSocialUserService[BasicProfile]  {

  override def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    collection.find(Json.obj("providerId" -> providerId, "userId" -> userId)).one[BasicProfile]
  }

  override def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    collection.find(Json.obj("providerId" -> providerId, "email" -> email)).one[BasicProfile]
  }

  override def save(profile: BasicProfile, mode: SaveMode): Future[BasicProfile] = {
    val id = JsString(profile.providerId + ":" + profile.userId)
    def doSave() = {
      val profileJson = Json.toJson(profile).as[JsObject] + ("_id" -> id)
      collection.insert(profileJson).filter(_.ok).map(_ => profile)
    }
    collection.find(Json.obj("_id" -> id)).
      one[BasicProfile].
      flatMap(_ match {
        case Some(profile) => Future.successful(profile)
        case None => doSave()
      })
  }

  override def link(current: BasicProfile, to: BasicProfile): Future[BasicProfile] = ???

  override def passwordInfoFor(user: BasicProfile): Future[Option[PasswordInfo]] = {
    Future.successful(user.passwordInfo)
  }

  override def updatePasswordInfo(user: BasicProfile, info: PasswordInfo): Future[Option[BasicProfile]] = {
    collection.update(Json.obj("providerId" -> user.providerId, "userId" -> user.userId),
      Json.obj("$set" -> Json.obj("passwordInfo" -> Json.toJson(info)))
    ).flatMap(_ => find(user.providerId, user.userId))
  }

  override def saveToken(token: MailToken): Future[MailToken] = ???

  override def findToken(token: String): Future[Option[MailToken]] = ???

  override def deleteToken(uuid: String): Future[Option[MailToken]] = ???

  override def deleteExpiredTokens(): Unit = ???
}
