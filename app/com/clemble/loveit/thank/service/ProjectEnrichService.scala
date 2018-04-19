package com.clemble.loveit.thank.service

import akka.actor.{ActorSystem, Scheduler}
import akka.pattern
import com.clemble.loveit.common.model.{DibsVerification, ProjectConstructor, Resource, Tag, UserID, WebStack}
import com.clemble.loveit.common.service.{UserService, WSClientAware}
import javax.inject.Inject
import org.jsoup.Jsoup
import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

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

trait ProjectWebStackAnalysis {

  def analyze(url: Resource): Future[Option[WebStack]]

}

case class WappalyzerWebStackAnalyzer(
  lookupUrl: String,
  client: WSClient
)(implicit ec: ExecutionContext) extends ProjectWebStackAnalysis with WSClientAware {

  override def analyze(url: Resource): Future[Option[WebStack]] = {
    client.url(lookupUrl)
      .addQueryStringParameters("url" -> url)
      .execute()
      .filter(_.status == 200)
      .map(resp => {
        val apps = (resp.json \ "applications").asOpt[List[JsObject]].getOrElse(List.empty[JsObject])
        val appNames = apps.map(_ \ "name").map(_.asOpt[WebStack]).flatten
        appNames.headOption
      })
      .recover({ case t => None })
  }

}

case class StaticWebStackAnalyzer(analyzedUrls: Map[Resource, WebStack]) extends ProjectWebStackAnalysis {

  override def analyze(url: Resource): Future[Option[WebStack]] = {
    Future.successful(analyzedUrls.get(url))
  }

}

case class SimpleProjectEnrichService @Inject()(rssFeedReader: RSSFeedReader, webStackAnalyzer: ProjectWebStackAnalysis, http: WSClient, userService: UserService, actorSystem: ActorSystem)(implicit ec: ExecutionContext) extends ProjectEnrichService {

  val DELAY: FiniteDuration = 3.second

  private def descriptionFromUrl(url: Resource): (Set[Tag], String, String) = {
    val title = if (url.startsWith("https"))
      url.substring(8, url.length)
    else
      url.substring(7, url.length)

    (Set.empty, title, url)
  }

  private def enrichDescription(url: Resource): Future[(Set[Tag], String, String)] = {
    http
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

  private def isValidFeed(feedUrl: Resource): Future[Boolean] = {
    rssFeedReader.read(feedUrl).map(_.nonEmpty).recover({ case _ => false })
  }

  private def enrichRSS(url: Resource): Future[Option[String]] = {
    val rssUrls = List(s"${url}/feed", s"${url}/rss")
    val possibleFeeds = Future.sequence(rssUrls.map(isValidFeed))
    possibleFeeds.map(rssResults => rssResults.zip(rssUrls).find(_._1 == true).map(_._2))
  }

  override def enrich(user: UserID, url: Resource): Future[ProjectConstructor] = {
    val fNone = pattern.after[Option[Nothing]](DELAY, actorSystem.scheduler)(Future.successful(None))
    val fWebStack = Future.firstCompletedOf(Seq(webStackAnalyzer.analyze(url), fNone))
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
        shortDescription = description,
        verification = DibsVerification,
        description = None,
        tags = tags,
        rss = rss
      )
    }
  }

}