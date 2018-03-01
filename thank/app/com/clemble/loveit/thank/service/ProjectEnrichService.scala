package com.clemble.loveit.thank.service

import javax.inject.Inject

import com.clemble.loveit.common.model.Tag
import com.clemble.loveit.thank.model.{Project, WebStack}
import com.clemble.loveit.user.service.UserService
import org.jsoup.Jsoup
import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient
import play.mvc.Http.Status

import scala.concurrent.{ExecutionContext, Future}

trait ProjectEnrichService {

  def enrich(project: Project): Future[Project]

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

}

case class SimpleProjectEnrichService @Inject()(lookupUrl: String, wsClient: WSClient, userService: UserService)(implicit ec: ExecutionContext) extends ProjectEnrichService {

  private def analyzeWebStack(url: String): Future[Option[WebStack]] = {
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

  private def enrichWebStack(project: Project): Future[Option[WebStack]] = {
    for {
      webStack <- analyzeWebStack(project.resource)
    } yield {
      webStack.orElse(project.webStack)
    }
  }

  private def enrichAvatar(project: Project): Future[Option[String]] = {
    if (project.avatar.isDefined) {
      return Future.successful(project.avatar)
    }
    userService.findById(project.user).map(_.flatMap(_.avatar))
  }

  private def enrichTags(project: Project): Future[Set[Tag]] = {
    if (project.tags.nonEmpty)
      return Future.successful(project.tags)
    wsClient
      .url(project.resource)
      .get()
      .map(resp => ProjectEnrichService.readTags(resp.body))
  }

  private def enrichDescription(project: Project): Future[Option[String]] = {
    if (project.description.isDefined)
      return Future.successful(project.description)
    wsClient
      .url(project.resource)
      .get()
      .map(resp => ProjectEnrichService.readDescription(resp.body))
  }

  private def enrichRSS(project: Project): Future[Option[String]] = {
    if (project.rss.isDefined)
      return Future.successful(project.rss)
    wsClient
      .url(project.resource)
      .get()
      .map(resp => {
        if (resp.status == Status.OK) {
          Some(project.resource)
        } else {
          None
        }
      })
  }

  override def enrich(project: Project): Future[Project] = {
    for {
      webStack <- enrichWebStack(project)
      avatar <- enrichAvatar(project)
      tags <- enrichTags(project)
      description <- enrichDescription(project)
      rss <- enrichRSS(project)
    } yield {
      project.copy(
        webStack = webStack,
        avatar = avatar,
        title = project.title.orElse(Some(project.resource)),
        description = description,
        tags = tags,
        rss = rss
      )
    }
  }

}