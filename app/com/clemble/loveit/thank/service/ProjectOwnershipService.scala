package com.clemble.loveit.thank.service

import java.net.URLEncoder

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.service.{TumblrProvider, UserOAuthService, UserService, WSClientAware}
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider.SpecifiedProfileError
import com.mohiva.play.silhouette.impl.providers.{OAuth1Info, OAuth2Info}
import javax.inject.{Inject, Singleton}
import WSClientAware._
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait ProjectOwnershipService {

  def fetch(user: UserID): Future[Seq[ProjectConstructor]]

}

@Singleton
case class TumblrProjectOwnershipService @Inject()(
  userService: UserService,
  oAuthService: UserOAuthService,
  tumblrProvider: TumblrProvider,
  client: WSClient,
  implicit val ec: ExecutionContext
) extends ProjectOwnershipService with WSClientAware {

  private def readTumblrResources(user: User, json: JsValue): Seq[ProjectConstructor] = {
    val blogs = (json \ "response" \ "user" \ "blogs").asOpt[List[JsObject]].getOrElse(List.empty[JsObject])

    blogs
      .filter(blog => (blog \ "admin").asOpt[Boolean].getOrElse(false))
      .map(blog => {

        val url = (blog \ "url").as[Resource].normalize()
        val title = (blog \ "title").asOpt[String].getOrElse("")
        val shortDescription = (blog \ "description").asOpt[String].getOrElse("")
        val rss = url + "/rss"

        ProjectConstructor(
          url = url,
          title = title,
          shortDescription = shortDescription,
          webStack = Some(Tumblr),
          rss = Some(rss),
          verification = TumblrVerification,
          avatar = user.avatar
        )
      })
  }

  override def fetch(user: UserID): Future[Seq[ProjectConstructor]] = {
    (for {
      userOpt <- userService.findById(user)
      tumblrLoginOpt = userOpt.flatMap(_.profiles.asTumblrLogin())
      tumblrAuthOpt <- tumblrLoginOpt.map(oAuthService.findAuthInfo).getOrElse(Future.successful(None))
    } yield {
      tumblrAuthOpt match {
        case Some(info: OAuth1Info) =>
          val url = s"https://api.tumblr.com/v2/user/info"
          client
            .url(url)
            .sign(tumblrProvider.service.sign(info))
            .get()
            .map(res => readTumblrResources(userOpt.get, res.json))
        case _ =>
          Future.successful(Seq.empty[ProjectConstructor])
      }
    }).flatten
  }

}

case class GoogleProjectOwnershipService @Inject()(
  userService: UserService,
  oAuthService: UserOAuthService,
  tumblrProvider: TumblrProvider,
  enrichService: ProjectEnrichService,
  client: WSClient,
  implicit val ec: ExecutionContext
) extends ProjectOwnershipService with WSClientAware {

  val ANDROID_APP = "ANDROID_APP"
  val INET_DOMAIN = "INET_DOMAIN"
  val SITE = "SITE"

  private def readGoogleResources(user: UserID, json: JsValue): Seq[Resource] = {
    (json \ "error").asOpt[JsObject].foreach(error => {
      val errorCode = (error \ "code").as[Int]
      val errorMsg = (error \ "message").as[String]

      throw new ProfileRetrievalException(SpecifiedProfileError.format(GoogleProvider.ID, errorCode, errorMsg))
    })

    val resources = (json \ "items" \\ "site").flatMap(site => {
      (site \ "type").as[String] match {
        case INET_DOMAIN =>
          val domain = (site \ "identifier").as[String]
          List(s"http://${domain}", s"https://${domain}")
        case SITE =>
          val url = (site \ "identifier").as[Resource].normalize()
          List(url)
        case _ =>
          List.empty
      }
    })

    resources
  }

  override def fetch(user: UserID): Future[Seq[ProjectConstructor]] = {
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
            .flatMap(urls => {
              val enrichedUrls = urls.map(url => {
                client.isAlive(url).flatMap({
                  case true => enrichService.enrich(user, url).map(Some(_))
                  case false => Future.successful(None)
                })
              })
              Future.sequence(enrichedUrls).map(_.flatten)
            })
            .map(_.map(_.copy(verification = GoogleVerification)))
        case _ =>
          Future.successful(Seq.empty[ProjectConstructor])
      }
    }).flatten
  }

}

@Singleton
case class SimpleProjectOwnershipService @Inject()(
  googleOwnershipService: GoogleProjectOwnershipService,
  tumblrOwnershipService: TumblrProjectOwnershipService,
  implicit val ec: ExecutionContext
) extends ProjectOwnershipService {

  override def fetch(user: UserID): Future[Seq[ProjectConstructor]] = {
    val fResources = Seq(googleOwnershipService, tumblrOwnershipService).map(_.fetch(user))
    Future.sequence(fResources).map(_.flatten)
  }

}

