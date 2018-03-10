package com.clemble.loveit.thank.service

import javax.inject.Inject

import akka.actor.Scheduler
import akka.pattern
import com.clemble.loveit.common.model.{Resource, Tag, UserID}
import com.clemble.loveit.thank.model.{ProjectConstructor, WebStack}
import com.clemble.loveit.user.service.UserService
import org.jsoup.Jsoup
import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient
import play.mvc.Http.Status

import scala.concurrent.duration._
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}

trait ProjectEnrichService {

  def enrich(user: UserID, url: Resource): Future[ProjectConstructor]

}

object ProjectEnrichService {


  def readTags(html: String): Set[Tag] = {
    val keywords = Option(Jsoup.parse(html).getElementsByAttributeValue("name", "keywords").first())
    keywords.flatMap(keywords => Option(keywords.attr("content"))).map(_.split(",").map(_.trim).toSet[String]).getOrElse(Set.empty)
  }

  def readDescription(html: String): Option[String] = {
    val description = Option(Jsoup.parse(html).getElementsByAttributeValue("name", "description").first())
    description.flatMap(keywords => Option(keywords.attr("content"))).filter(!_.isEmpty)
  }

  def readTitle(html: String): Option[String] = {
    val title = Option(Jsoup.parse(html).getElementsByTag("title").first())
    title.flatMap(title => Option(title.`val`())).filterNot(_.isEmpty)
  }

}

case class SimpleProjectEnrichService @Inject()(lookupUrl: String, wsClient: WSClient, userService: UserService, scheduler: Scheduler)(implicit ec: ExecutionContext) extends ProjectEnrichService {

  val cache = new mutable.WeakHashMap[String, ProjectConstructor]()
  val DELAY = 3.second

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

  private def descriptionFromUrl(url: Resource): (Set[Tag], String, String) = {
    val title = if (url.startsWith("https"))
      url.substring(8, url.length - 1)
    else
      url.substring(7, url.length - 1)

    (Set.empty, title, url)
  }

  private def enrichDescription(url: Resource): Future[(Set[Tag], String, String)] = {
    wsClient
      .url(url)
      .get()
      .map(resp => {
        val (_, defaultTitle, defaultDescription) = descriptionFromUrl(url)
        val tags = ProjectEnrichService.readTags(resp.body)
        val description = ProjectEnrichService.readDescription(resp.body).getOrElse(defaultDescription)
        val title = ProjectEnrichService.readTitle(resp.body).getOrElse(defaultTitle)

        (tags, description, title)
      })
  }

  private def enrichRSS(url: Resource): Future[Option[String]] = {
    val rssUrls = List(s"${url}feed", s"${url}rss")
    val possibleFeeds = Future.sequence(rssUrls.map(rssUrl => wsClient.url(rssUrl).get()))
    possibleFeeds.map(rssResults => rssResults.zip(rssUrls).find(_._1.status == Status.OK).map(_._2))
  }

  override def enrich(user: UserID, url: Resource): Future[ProjectConstructor] = {
    val fRes = cache.get(url) match {
      case Some(prj) => Future.successful(prj)
      case None =>
        val fNone = pattern.after[Option[Nothing]](DELAY, scheduler)(Future.successful(None))
        val fWebStack = Future.firstCompletedOf(Seq(enrichWebStack(url), fNone))
        val fDescription = Future.firstCompletedOf(Seq(enrichDescription(url), fNone.map(_ => descriptionFromUrl(url))))
        val fRss = Future.firstCompletedOf(Seq(enrichRSS(url), fNone))
        val fAvatar = userService.findById(user).map(_.flatMap(_.avatar))
        for {
          webStack <- fWebStack
          (tags, description, title) <- fDescription
          avatar <- fAvatar
          rss <- fRss
        } yield {
          ProjectConstructor(
            url = url,
            avatar = avatar,
            webStack = webStack,
            title = title,
            description = description,
            tags = tags,
            rss = rss
          )
        }
    }
    fRes.foreach(prj => cache.put(url, prj))
    fRes
  }

}