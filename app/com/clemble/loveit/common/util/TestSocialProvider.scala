package com.clemble.loveit.common.util

import java.util.concurrent.ConcurrentHashMap

import com.clemble.loveit.user.model.User.socialProfileJsonFormat
import com.mohiva.play.silhouette.api.util.{ExtractableRequest, HTTPLayer}
import com.mohiva.play.silhouette.impl.providers._
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsJson, Result}

import scala.concurrent.Future

case class TestSocialProvider(
                               httpLayer: HTTPLayer,
                               stateProvider: SocialStateHandler,
                               stateHandler: SocialStateHandler,
                               settings: OAuth2Settings
  ) extends OAuth2Provider with CommonSocialProfileBuilder {

  override type Content = JsValue

  override protected def urls: Map[String, String] = Map.empty

  override val id = "test"
  override type Self = TestSocialProvider

  val cache = new ConcurrentHashMap[String, CommonSocialProfile]()

  override def authenticate[B]()(implicit request: ExtractableRequest[B]): Future[Either[Result, OAuth2Info]] = {
    val user = request.body match {
      case AnyContentAsJson(json) => json.as[CommonSocialProfile]
    }
    cache.put(user.loginInfo.providerKey, user)
    Future.successful(Right(OAuth2Info(accessToken = user.loginInfo.providerKey)))
  }

  override def withSettings(f: (Settings) => Settings) = {
    new TestSocialProvider(httpLayer, stateHandler, stateProvider, f(settings))
  }

  override protected def buildProfile(authInfo: OAuth2Info): Future[Profile] = {
    Future.successful(cache.remove(authInfo.accessToken))
  }

  override protected def profileParser: SocialProfileParser[JsValue, Profile, OAuth2Info] = ???

}
