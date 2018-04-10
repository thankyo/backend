package com.clemble.loveit.thank.service

import java.net.URLEncoder

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.service.{TumblrProvider, UserOAuthService, UserService}
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider.SpecifiedProfileError
import com.mohiva.play.silhouette.impl.providers.{OAuth1Info, OAuth2Info}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait ProjectOwnershipService {

  def fetch(user: UserID): Future[Seq[Resource]]

}

@Singleton
case class SimpleProjectOwnershipService @Inject()(
  userService: UserService,
  oAuthService: UserOAuthService,
  tumblrProvider: TumblrProvider,
  client: WSClient,
  implicit val ec: ExecutionContext
) extends ProjectOwnershipService {

  private def readGoogleResources(user: UserID, json: JsValue): Seq[Resource] = {
    (json \ "error").asOpt[JsObject].foreach(error => {
      val errorCode = (error \ "code").as[Int]
      val errorMsg = (error \ "message").as[String]

      throw new ProfileRetrievalException(SpecifiedProfileError.format(GoogleProvider.ID, errorCode, errorMsg))
    })

    val resources = (json \ "items" \\ "site").map(site => (site \ "identifier").as[Resource].normalize())

    resources
  }

  private def fetchGoogleResources(user: UserID): Future[Seq[Resource]] = {
    (for {
      googleLogin <- userService.findById(user).map(_.flatMap(_.profiles.asGoogleLogin()))
      googleAuthOpt <- googleLogin.map(oAuthService.findAuthInfo).getOrElse(Future.successful(None))
    } yield {
      googleAuthOpt match {
        case Some(info: OAuth2Info) =>
          val url = s"https://www.googleapis.com/siteVerification/v1/webResource?access_token=${URLEncoder.encode(info.accessToken, "UTF-8")}"
          client
            .url(url)
            .withHttpHeaders("Authorization" -> s"Bearer ${info.accessToken}")
            .get()
            .map(res => readGoogleResources(user, res.json))
        case None =>
          Future.successful(Seq.empty[Resource])
      }
    }).flatten
  }

  private def readTumblrResources(user: UserID, json: JsValue): Seq[Resource] = {
    val blogs = (json \ "response" \ "user" \ "blogs").asOpt[List[JsObject]].getOrElse(List.empty[JsObject])
    blogs
      .filter(blog => (blog \ "admin").asOpt[Boolean].getOrElse(false))
      .map(blog => (blog \ "url").asOpt[Resource].map(_.normalize()))
      .flatten
  }

  private def fetchTumblrResources(user: UserID): Future[Seq[Resource]] = {
    (for {
      tumblrLogin <- userService.findById(user).map(_.flatMap(_.profiles.asTumblrLogin()))
      tumblrAuthOpt <- tumblrLogin.map(oAuthService.findAuthInfo).getOrElse(Future.successful(None))
    } yield {
      tumblrAuthOpt match {
        case Some(info: OAuth1Info) =>
          val url = s"https://api.tumblr.com/v2/user/info"
          client
            .url(url)
            .sign(tumblrProvider.service.sign(info))
            .get()
            .map(res => readTumblrResources(user, res.json))
        case _ =>
          Future.successful(Seq.empty[Resource])
      }
    }).flatten
  }

  override def fetch(user: UserID): Future[Seq[Resource]] = {
    val googleRes = fetchGoogleResources(user)
    val tumblrRes = fetchTumblrResources(user)
    Future.sequence(Seq(googleRes, tumblrRes)).map(_.flatten)
  }

}

