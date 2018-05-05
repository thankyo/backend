package com.clemble.loveit.thank.service

import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.UUID

import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.service.{EmailService, _}
import com.clemble.loveit.thank.model.UserProject
import com.clemble.loveit.thank.service.repository.{DibsProjectOwnershipRepository, UserProjectRepository}
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider.SpecifiedProfileError
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait ProjectOwnershipService {

}

@Singleton
case class TumblrProjectOwnershipService @Inject()(
  api: TumblrAPI,
  oAuthService: UserOAuthService,
  repo: UserProjectRepository,
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

        val avatar = s"https://api.tumblr.com/v2/blog/${url.toParentDomain()}/avatar/128"

        OwnedProject(
          url = url,
          title = title,
          shortDescription = shortDescription,
          webStack = Some(Tumblr),
          rss = Some(rss),
          avatar = Some(avatar)
        )
      })
  }

  private def fetchProjects(user: UserID): Future[Seq[OwnedProject]] = {
    api.findUser(user).map({
      case Some(json) => readTumblrResources(json)
      case None => Seq.empty[OwnedProject]
    })
  }

  def refresh(user: UserID): Future[UserProject] = {
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
  repo: UserProjectRepository,
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


  def refresh(user: UserID): Future[UserProject] = {
    for {
      urls <- fetchOwned(user)
      projects <- Future.sequence(urls.map(enrichService.enrich(user, _)))
      userProjects <- repo.saveGoogleProjects(user, projects)
    } yield {
      userProjects
    }
  }

}

case class DibsProjectOwnershipToken(
  user: UserID,
  email: Email,
  url: Resource,
  token: UUID = UUID.randomUUID(),
  created: LocalDateTime = LocalDateTime.now()
) extends Token with ResourceAware

object DibsProjectOwnershipToken {

  implicit val jsonFormat = Json.format[DibsProjectOwnershipToken]

}

@Singleton
case class DibsProjectOwnershipService @Inject()(
  urlValidator: URLValidator,
  enrichService: ProjectEnrichService,
  whoisService: WHOISService,
  emailService: EmailService,
  tokenRepo: TokenRepository[DibsProjectOwnershipToken],
  repo: DibsProjectOwnershipRepository,
  implicit val ec: ExecutionContext
) extends ProjectOwnershipService {

  def sendVerification(user: UserID, url: Resource): Future[Option[Email]] = {
    for {
      whoisEmailOpt <- repo.findDibsProject(user).map(_.find(_.url == url).flatMap(_.whoisEmail))
      emailOpt <- whoisEmailOpt.
        map(email => Future.successful(Some(email))).
        getOrElse(whoisService.fetchEmail(url))
      sent <- emailOpt.
        map(email => tokenRepo.
          save(DibsProjectOwnershipToken(user, url, email)).
          flatMap(token => emailService.sendWHOISVerificationEmail(token))
        ).getOrElse(Future.successful(false))
    } yield {
      emailOpt.filter(_ => sent)
    }
  }

  def verify(user: UserID, token: UUID): Future[UserProject] = {
    for {
      tokenOpt <- tokenRepo.findAndRemoveByToken(token)
      _ = if (tokenOpt.isEmpty) throw new IllegalArgumentException("Token expired regenerate it")
      token = tokenOpt.get
      _ = if (token.user != user) throw new IllegalArgumentException("Generated by different user")
      usrPrj <- repo.validateDibsProject(token.user, token.url)
    } yield {
      usrPrj
    }
  }

  def create(user: UserID, url: Resource): Future[UserProject] = {
    for {
      urlOpt <- urlValidator.findAlive(url)
      _ = if (urlOpt.isEmpty) throw FieldValidationError("url", "Can't connect")
      baseProject <- enrichService.enrich(user, urlOpt.get)
      emailOpt <- whoisService.fetchEmail(urlOpt.get)
      dibsProject = baseProject.asDibsProject(emailOpt)
      usrPrj <- repo.saveDibsProjects(user, Seq(dibsProject))
    } yield {
      sendVerification(user, urlOpt.get)
      usrPrj
    }
  }

  def delete(user: UserID, url: Resource): Future[UserProject] = {
    for {
      userProject <- repo.deleteDibsProject(user, url)
    } yield {
      userProject
    }
  }

}

