package com.clemble.loveit.thank.service

import java.time.LocalDateTime

import com.clemble.loveit.common.model
import com.clemble.loveit.common.model.{OpenGraphImage, OpenGraphObject, Project, ProjectPointer, Resource, Tag, Tumblr}
import com.clemble.loveit.common.service.WSClientAware
import javax.inject.{Inject, Singleton}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait PostEnrichService {

  def enrich(prj: ProjectPointer, post: OpenGraphObject): Future[OpenGraphObject]

}

sealed trait PostSourceService {

  def fetch(prj: ProjectPointer, url: Resource): Future[Option[OpenGraphObject]]

}

@Singleton
case class FacebookPostSourceService @Inject()(
  client: WSClient,
  implicit val ec: ExecutionContext
) extends PostSourceService with WSClientAware {

  private def openGraphFromFB(url: String, fb: JsValue): Option[OpenGraphObject] = {
    val imageUrl = (fb \ "og_object" \ "image" \ "src").asOpt[String]
    val description = (fb \ "og_object" \ "description").asOpt[String]
    val title = (fb \ "og_object" \ "title").asOpt[String]
    val pubDate = (fb \ "og_object" \ "update_date").asOpt[LocalDateTime]

    if (imageUrl.isDefined || description.isDefined || title.isDefined) {
      Some(
        model.OpenGraphObject(
          url,
          image = imageUrl.map(OpenGraphImage(_)),
          description = description,
          title = title,
          pubDate = pubDate
        )
      )
    } else {
      None
    }
  }

  override def fetch(prj: ProjectPointer, url: String): Future[Option[OpenGraphObject]] = {
    client.url(s"https://graph.facebook.com/?id=${url}")
      .get()
      .map(res => {
        if (res.status == Status.OK) {
          openGraphFromFB(url, res.json)
        } else {
          None
        }
      }).
      recover({ case _ => None })
  }

}

@Singleton
case class HtmlPostSourceService @Inject()(
  client: WSClient,
  implicit val ec: ExecutionContext
) extends PostSourceService with WSClientAware {

  private def openGraphFromHTML(url: String, htmlStr: String): Option[OpenGraphObject] = {
    Option(Jsoup.parse(htmlStr)).map(doc => {
      val imageUrl = {
        val ogImage = doc.getElementsByAttributeValue("property", "og:image").first()
        val imageSrc = doc.getElementsByAttributeValue("property", "og:image:src").first()
        Option(ogImage).orElse(Option(imageSrc)).map(_.attr("content"))
      }

      val description = {
        val descriptionProp = doc.getElementsByAttributeValue("property", "og:description").first()
        Option(descriptionProp).map(_.attr("content"))
      }

      val title = {
        val titleProp = doc.getElementsByAttributeValue("property", "og:title").first()
        val title = Option(doc.getElementsByTag("title").first()).map(_.text())
        Option(titleProp).map(_.attr("content")).orElse(title)
      }

      model.OpenGraphObject(
        url,
        description = description,
        title = title,
        image = imageUrl.map(OpenGraphImage(_))
      )
    })
  }

  private def isRedirect(status: Int) = {
    status match {
      case Status.MOVED_PERMANENTLY => true
      case Status.TEMPORARY_REDIRECT => true
      case Status.PERMANENT_REDIRECT => true
      case _ => false
    }
  }

  private def getPostHtml(postUrl: String, allowRedirect: Boolean = true): Future[Option[String]] = {
    client
      .url(postUrl).withFollowRedirects(false)
      .get
      .flatMap(res => {
        if (res.status == Status.OK) {
          Future.successful(Some(res.body))
        } else {
          res.header("Location") match {
            case Some(redirect) if isRedirect(res.status) && allowRedirect =>
              val normRedirect = if (redirect.indexOf("?") != -1) redirect.substring(0, redirect.indexOf("?")) else redirect
              getPostHtml(normRedirect, false)
            case _ =>
              Future.successful(Option.empty[String])
          }
        }
      })
  }

  override def fetch(prj: ProjectPointer, url: String): Future[Option[OpenGraphObject]] = {
    getPostHtml(url).
      map(htmlOpt => {
        htmlOpt.flatMap(openGraphFromHTML(url, _))
      }).
      recover({ case _ => None })
  }

}

@Singleton
case class TumblrPostSourceService @Inject()(
  tumblrAPI: TumblrAPI,
  implicit val ec: ExecutionContext
) extends PostSourceService {

  private def openGraphFromTumblr(url: String, json: JsObject): Option[OpenGraphObject] = {
    val pubDate = (json \ "date").asOpt[LocalDateTime]
    val tags = (json \ "tags").asOpt[Set[Tag]].getOrElse(Set.empty)
    val title = (json \ "summary").asOpt[String]
    Some(
      OpenGraphObject(
        url = url,
        title = title,
        tags = tags,
        pubDate = pubDate
      )
    )
  }

  override def fetch(prj: ProjectPointer, url: String): Future[Option[OpenGraphObject]] = {
    if (!prj.webStack.contains(Tumblr)) {
      return Future.successful(None)
    }
    val parts = url.split("/")
    val blog = parts(2)
    val postId = parts(4)
    tumblrAPI
      .findPost(prj.user, blog, postId)
      .map(_.flatMap(jsObj => openGraphFromTumblr(url, jsObj))).
      recover({ case _ => None })
  }

}


@Singleton
case class SimplePostEnrichService @Inject()(
  fbEnrichService: FacebookPostSourceService,
  htmlEnrichService: HtmlPostSourceService,
  tumblrEnrichService: TumblrPostSourceService,
  implicit val ec: ExecutionContext
) extends PostEnrichService {

  override def enrich(prj: ProjectPointer, post: OpenGraphObject): Future[OpenGraphObject] = {
    val fbGraph = fbEnrichService.fetch(prj, post.url)
    val htmlGraph = htmlEnrichService.fetch(prj, post.url)
    val tumblrGraph = tumblrEnrichService.fetch(prj, post.url)

    for {
      fb <- fbGraph
      html <- htmlGraph
      tumblr <- tumblrGraph
    } yield {
      val base = tumblr.orElse(fb).orElse(html).getOrElse(post)
      base.merge(fb).merge(html)
    }
  }

}

case object TestPostEnrichService extends PostEnrichService {
  override def enrich(prj: ProjectPointer, post: OpenGraphObject): Future[OpenGraphObject] = {
    Future.successful(post)
  }
}