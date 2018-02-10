package com.clemble.loveit.thank.service

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider.SpecifiedProfileError
import play.api.libs.json.{JsObject, JsValue}

import scala.concurrent.{ExecutionContext, Future}

trait ROService {

  def list(user: UserID): Future[List[SupportedProject]]

  def validate(prj: SupportedProject): Future[SupportedProject]

}

@Singleton
case class SimpleROService @Inject()(
                                      supportedProjectService: SupportedProjectService,
                                      postService: PostService,
                                      userService: UserService,
                                      httpLayer: HTTPLayer,
                                      authService: AuthInfoRepository,
                                      implicit val ec: ExecutionContext
                                    ) extends ROService {

  def readGoogleResources(user: UserID, json: JsValue): List[SupportedProject] = {
    (json \ "error").asOpt[JsObject].foreach(error => {
      val errorCode = (error \ "code").as[Int]
      val errorMsg = (error \ "message").as[String]

      throw new ProfileRetrievalException(SpecifiedProfileError.format(GoogleProvider.ID, errorCode, errorMsg))
    })

    val resources = (json \ "items")
      .asOpt[List[JsObject]]
      .map(_.map(_ \ "site" \ "identifier").map(_.asOpt[String]).flatten)
      .getOrElse(List.empty[String])

    resources.map(res => SupportedProject(Resource.from(res), user))
  }

  override def list(user: UserID): Future[List[SupportedProject]] = {
    (for {
      googleLogin <- userService.findById(user).map(_.flatMap(_.profiles.google.map(googleKey => LoginInfo(GoogleProvider.ID, googleKey))))
      googleAuthOpt <- googleLogin.map(authService.find[OAuth2Info]).getOrElse(Future.successful(None))
    } yield {
      googleAuthOpt match {
        case Some(OAuth2Info(accessToken, _, _, _, _)) =>
          val url = s"https://www.googleapis.com/siteVerification/v1/webResource?access_token=${URLEncoder.encode(accessToken, "UTF-8")}"
          httpLayer
            .url(url)
            .get()
            .map(res => readGoogleResources(user, res.json))
        case None =>
          Future.successful(List.empty[SupportedProject])
      }
    }).flatten
  }

  override def validate(project: SupportedProject): Future[SupportedProject] = {
    // TODO assign is internal operation, so it might not need to throw Exception,
    // since verification has already been done before
    for {
      created <- supportedProjectService.create(project)
      _ = if (!created) throw new IllegalArgumentException("Could not create project")
      updPosts <- postService.updateOwner(project) if (updPosts)
      _ = if (!updPosts) throw new IllegalArgumentException("Failed to update posts")
    } yield {
      if (!updPosts)
        throw new IllegalArgumentException("Can't assign ownership")
      project
    }
  }

}
