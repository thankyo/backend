package com.clemble.loveit.thank.service

import javax.inject.Inject

import com.clemble.loveit.common.model.{Resource, Tag, UserID}
import com.clemble.loveit.thank.model.{Project, WebStack}
import org.jsoup.Jsoup
import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient
import play.mvc.Http.Status

import scala.concurrent.{ExecutionContext, Future}

trait ProjectEnrichService {

  def enrich(user: UserID, url: Resource): Future[Project]

}

object ProjectEnrichService {

  def readTags(html: String): Set[Tag] = {
    val keywords = Option(Jsoup.parse(html).getElementsByAttributeValue("name", "keywords").first())
    keywords.flatMap(keywords => Option(keywords.attr("content"))).map(_.split(",").map(_.trim).toSet[String]).getOrElse(Set.empty)
  }

  def readDescription(html: String): Option[String] = {
    val description = Option(Jsoup.parse(html).getElementsByAttributeValue("name", "description").first())
    description.flatMap(keywords => Option(keywords.attr("content")))
  }

  def readTitle(html: String): Option[String] = {
    val title = Option(Jsoup.parse(html).getElementsByTag("title").first())
    title.flatMap(title => Option(title.`val`()))
  }

}

case class SimpleProjectEnrichService @Inject()(lookupUrl: String, wsClient: WSClient)(implicit ec: ExecutionContext) extends ProjectEnrichService {

  private def enrichWebStack(url: Resource): Future[Option[WebStack]] = {
    wsClient.url(lookupUrl)
      .addQueryStringParameters("url" -> url)
      .execute()
      .filter(_.status == 200)
      .map(resp => {
        val apps = (resp.json \ "applications").asOpt[List[JsObject]].getOrElse(List.empty[JsObject])
        val appNames = apps.map(_ \ "name").map(_.asOpt[WebStack]).flatten
        appNames.headOption
      })
  }

  private def enrichDescription(url: Resource): Future[(Set[Tag], Option[String], Option[String])] = {
    wsClient
      .url(url)
      .get()
      .map(resp => {
        val tags = ProjectEnrichService.readTags(resp.body)
        val description = ProjectEnrichService.readDescription(resp.body)
        val title = ProjectEnrichService.readTitle(resp.body)

        (tags, description, title)
      })
  }

  private def enrichRSS(url: Resource): Future[Option[String]] = {
    wsClient
      .url(url)
      .get()
      .map(resp => {
        if (resp.status == Status.OK) {
          Some(url)
        } else {
          None
        }
      })
  }

  override def enrich(user: UserID, url: Resource): Future[Project] = {
    val fWebStack = enrichWebStack(url)
    val fDescription = enrichDescription(url)
    val fRss = enrichRSS(url)
    for {
      webStack <- fWebStack
      (tags, description, title) <- fDescription
      rss <- fRss
    } yield {
      Project(
        url = url,
        user = user,
        webStack = webStack,
        title = title.orElse(Some(url)),
        description = description,
        tags = tags,
        rss = rss
      )
    }
  }

}