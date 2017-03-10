package com.clemble.thank

import com.clemble.thank.social.auth.{MongoSecureSocialUserService, SecureSocialRuntimeEnvironment}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Inject, Provides}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection
import securesocial.core.services.{UserService => SecureSocialUserService}
import securesocial.core._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class SecureSocialModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[RuntimeEnvironment]).to(classOf[SecureSocialRuntimeEnvironment])
  }

  @Provides
  @Named("secureSocial")
  def secureSocialMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection]("secureSocial", FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }

  @Provides
  @Named("secureSocialUserService")
  def userService(@Named("secureSocial") collection: JSONCollection, ec: ExecutionContext): SecureSocialUserService[BasicProfile] = {
    implicit val authMethodFormat = Json.format[AuthenticationMethod]
    implicit val oauth1InfoFormat = Json.format[OAuth1Info]
    implicit val oauth2InfoFormat = Json.format[OAuth2Info]
    implicit val passwordInfoFormat = Json.format[PasswordInfo]
    val basicProfileFormat = Json.format[BasicProfile]
    MongoSecureSocialUserService(collection, ec, basicProfileFormat, passwordInfoFormat)
  }

}
