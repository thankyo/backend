package com.clemble.loveit.thank.service

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.auth.service.{UserOAuthService}
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider.SpecifiedProfileError
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
                                                       client: WSClient,
                                                       implicit val ec : ExecutionContext
                                                    ) extends ProjectOwnershipService {

  private def readGoogleResources(user: UserID, json: JsValue): Seq[Resource] = {
    (json \ "error").asOpt[JsObject].foreach(error => {
      val errorCode = (error \ "code").as[Int]
      val errorMsg = (error \ "message").as[String]

      throw new ProfileRetrievalException(SpecifiedProfileError.format(GoogleProvider.ID, errorCode, errorMsg))
    })

    val resources = (json \ "items" \\ "site")
      .map(site => (site \ "identifier").as[Resource] match {
        case url if url.startsWith("http") => url
        case url => s"https://${url}/"
      })

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

  override def fetch(user: UserID): Future[Seq[Resource]] = {
    fetchGoogleResources(user)
  }

}

