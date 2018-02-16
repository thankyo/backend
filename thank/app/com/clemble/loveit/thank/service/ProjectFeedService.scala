package com.clemble.loveit.thank.service

import javax.inject.Inject

import com.clemble.loveit.thank.model.{OpenGraphImage, OpenGraphObject, Post, Project}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, XML}

trait ProjectFeedService {

  def refresh(project: Project): Future[List[Post]]

}

object ProjectFeedService {

  def readRSS(feed: Elem): List[OpenGraphObject] = {
    val items = feed \ "channel" \ "item"
    val ogObj = items.map(item => {
      Option((item \ "link").text).map(link =>
        OpenGraphObject(
          url = link,
          title = Option((item \ "title").text)
        )
      )
    })
    ogObj.flatten.toList
  }


  def readOpenGraph(origGO: OpenGraphObject, htmlStr: String, fb: JsValue = Json.obj()): OpenGraphObject = {
    Option(Jsoup.parse(htmlStr)) match {
      case Some(html) =>
        val possibleImageUrl = (fb \ "og_object" \ "image" \ "src").asOpt[String].orElse(
          Option(html.getElementsByAttributeValue("property", "og:image").first())
            .orElse(Option(html.getElementsByAttributeValue("property", "og:image:src").first()))
            .map(_.attr("content"))
        )
        val possibleDescription = (fb \ "og_object" \ "description").asOpt[String].orElse(
          Option(html.getElementsByAttributeValue("property", "og:description").first())
            .map(_.attr("content"))
        )
        val possibleTitle = (fb \ "og_object" \ "title").asOpt[String].orElse(
          Option(html.getElementsByAttributeValue("property", "og:title").first()).map(_.attr("property"))
        )

        origGO.copy(
          image = origGO.image.orElse(possibleImageUrl.map(OpenGraphImage(_))),
          description = origGO.description.orElse(possibleDescription),
          title = origGO.title.orElse(possibleTitle)
        )
      case None =>
        origGO
    }
  }

}

case class SimpleProjectFeedService @Inject()(wsClient: WSClient, postService: PostService, implicit val ec: ExecutionContext) extends ProjectFeedService {

  import ProjectFeedService._

  private def fetch(project: Project): Future[List[OpenGraphObject]] = {
    project.rss match {
      case Some(feedUrl) =>
        wsClient.url(feedUrl).get()
          .map(feed => readRSS(XML.loadString(feed.body)))
          .map(_.map(ogObj => if (ogObj.tags.isEmpty) ogObj.copy(tags = project.tags) else ogObj))
      case None =>
        Future.successful(List.empty)
    }
  }

  private def isRedirect(res: WSResponse) = {
    res.status match {
      case Status.MOVED_PERMANENTLY => true
      case Status.TEMPORARY_REDIRECT => true
      case Status.PERMANENT_REDIRECT => true
      case _ => false
    }
  }

  private def enrich(ogObj: OpenGraphObject, allowRedirect: Boolean = true): Future[OpenGraphObject] = {
    wsClient
      .url(ogObj.url).withFollowRedirects(false)
      .get
      .flatMap(res => {
        res.header("Location").headOption match {
          case Some(url) if (isRedirect(res) && allowRedirect) =>
            val normUrl = if (url.indexOf("?") != -1) url.substring(0, url.indexOf("?")) else url
            enrich(ogObj.copy(url = normUrl), false)
          case None =>
            wsClient.url(s"https://graph.facebook.com/?id=${ogObj.url}")
              .get()
              .map(_.json)
              .map(fb => readOpenGraph(ogObj, res.body, fb))
        }
      })
  }

  override def refresh(project: Project): Future[List[Post]] = {
    for {
      fetched <- fetch(project)
      enriched <- Future.sequence(fetched.map(ogObj => enrich(ogObj)))
      validPosts = enriched.filter(_.url.contains(project.resource.uri)) // TODO should be startsWith check
      created <- Future.sequence(validPosts.map(postService.create))
    } yield {
      created
    }
  }

}