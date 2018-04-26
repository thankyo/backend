package com.clemble.loveit.thank.service

import java.net.URLEncoder

import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.service._
import com.clemble.loveit.thank.model.UserProjects
import com.clemble.loveit.thank.service.repository.UserProjectsRepository
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider.SpecifiedProfileError
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait ProjectOwnershipService {

  def refresh(user: UserID): Future[UserProjects]

}

@Singleton
case class TumblrProjectOwnershipService @Inject()(
  api: TumblrAPI,
  oAuthService: UserOAuthService,
  repo: UserProjectsRepository,
  client: WSClient,
  implicit val ec: ExecutionContext
) extends ProjectOwnershipService with WSClientAware {

  private def readTumblrResources(json: JsValue): Seq[OwnedProject] = {
    val blogs = (json \ "response" \ "user" \ "blogs").asOpt[List[JsObject]].getOrElse(List.empty[JsObject])

    blogs
      .filter(blog => (blog \ "admin").asOpt[Boolean].getOrElse(false))
      .map(blog => {

        val url = (blog \ "url").as[Resource].normalize()
        val title = (blog \ "title").asOpt[String].getOrElse("")
        val shortDescription = (blog \ "description").asOpt[String].getOrElse("")
        val rss = url + "/rss"

        OwnedProject(
          url = url,
          title = title,
          shortDescription = shortDescription,
          webStack = Some(Tumblr),
          rss = Some(rss)
        )
      })
  }

  private def fetchProjects(user: UserID): Future[Seq[OwnedProject]] = {
    api.findUser(user).map({
      case Some(json) => readTumblrResources(json)
      case None => Seq.empty[OwnedProject]
    })
  }

  override def refresh(user: UserID): Future[UserProjects] = {
    for {
      projects <- fetchProjects(user)
      userProjects <- repo.saveTumblrProjects(user, projects)
    } yield {
      userProjects
    }
  }

}

case class GoogleProjectOwnershipService @Inject()(
  oAuthService: UserOAuthService,
  enrichService: ProjectEnrichService,
  urlValidator: URLValidator,
  repo: UserProjectsRepository,
  client: WSClient,
  implicit val ec: ExecutionContext
) extends ProjectOwnershipService with WSClientAware {

  val ANDROID_APP = "ANDROID_APP"
  val INET_DOMAIN = "INET_DOMAIN"
  val SITE = "SITE"

  private def readGoogleResources(json: JsValue): Seq[Resource] = {
    (json \ "error").asOpt[JsObject].foreach(error => {
      val errorCode = (error \ "code").as[Int]
      val errorMsg = (error \ "message").as[String]

      throw new ProfileRetrievalException(SpecifiedProfileError.format(GoogleProvider.ID, errorCode, errorMsg))
    })

    val resources = (json \ "items" \\ "site").map(site => {
      (site \ "type").as[String] match {
        case INET_DOMAIN =>
          val domain = (site \ "identifier").as[String]
          domain
        case SITE =>
          val url = (site \ "identifier").as[Resource].normalize()
          url
      }
    })

    resources
  }


  private def fetchOwned(user: UserID): Future[Seq[Resource]] = {
    (for {
      googleAuthOpt <- oAuthService.findAuthInfo(user, GoogleProvider.ID)
    } yield {
      googleAuthOpt match {
        case Some(info: OAuth2Info) =>
          val url = s"https://www.googleapis.com/siteVerification/v1/webResource?access_token=${URLEncoder.encode(info.accessToken, "UTF-8")}"
          client
            .url(url)
            .withHttpHeaders("Authorization" -> s"Bearer ${info.accessToken}")
            .get()
            .map(res => if (res.status == 200) readGoogleResources(res.json) else Seq.empty)
            .flatMap(urls => Future.sequence(urls.map(url => urlValidator.findAlive(url))).map(_.flatten))
        case _ =>
          Future.successful(Seq.empty[Resource])
      }
    }).flatten
  }


  override def refresh(user: UserID): Future[UserProjects] = {
    for {
      urls <- fetchOwned(user)
      projects <- Future.sequence(urls.map(enrichService.enrich(user, _)))
      userProjects <- repo.saveGoogleProjects(user, projects)
    } yield {
      userProjects
    }
  }

}

@Singleton
case class DibsProjectOwnershipService @Inject()(
  urlValidator: URLValidator,
  enrichService: ProjectEnrichService,
  emailVerSvc: ProjectOwnershipByEmailService,
  repo: UserProjectsRepository,
  implicit val ec: ExecutionContext
) extends ProjectOwnershipService {

  def dibsOnUrl(user: UserID, url: Resource): Future[OwnedProject] = {
    for {
      urlOpt <- urlValidator.findAlive(url)
      _ = if (urlOpt.isEmpty) throw FieldValidationError("url", "Can't connect")
      ownedProject <- enrichService.enrich(user, urlOpt.get)
      _ <- repo.saveDibsProjects(user, Seq(ownedProject))
    } yield {
      emailVerSvc.verifyWithWHOIS(user, urlOpt.get)
      ownedProject
    }
  }

  override def refresh(user: UserID): Future[UserProjects] = {
    repo.findById(user).map(_.get)
  }

}

