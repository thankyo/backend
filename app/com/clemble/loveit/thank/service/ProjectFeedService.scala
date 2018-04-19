package com.clemble.loveit.thank.service

import java.time.LocalDateTime

import com.clemble.loveit.common.model.{OpenGraphObject, Post, Project}
import com.clemble.loveit.common.service.WSClientAware
import javax.inject.{Inject, Singleton}
import play.api.http.Status
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, XML}

trait ProjectFeedService {

  def refresh(project: Project): Future[List[Post]]

}

object RSSFeedReader {

  def readFeed(xml: String): List[OpenGraphObject] = readFeed(XML.loadString(xml))

  def readFeed(feed: Elem): List[OpenGraphObject] = FeedParser.parse(feed).toList

  implicit val pubDateOrdering: Ordering[Option[LocalDateTime]] = (x: Option[LocalDateTime], y: Option[LocalDateTime]) => {
    y.getOrElse(LocalDateTime.MIN).compareTo(x.getOrElse(LocalDateTime.MIN))
  }

}

@Singleton
case class RSSFeedReader @Inject()(
  client: WSClient,
  implicit val ec: ExecutionContext
) extends WSClientAware {

  import RSSFeedReader._

  def read(feedUrl: String): Future[List[OpenGraphObject]] = {
    client.url(feedUrl).get()
      .map(feed => {
        if (feed.status == Status.OK) {
          readFeed(feed.body).sortBy(_.pubDate)
        } else {
          List.empty
        }
      }).recover({
      case _ => List.empty[OpenGraphObject]
    })
  }

}

case class SimpleProjectFeedService @Inject()(rssFeedReader: RSSFeedReader, wsClient: WSClient, postEnrichService: PostEnrichService, postService: PostService, implicit val ec: ExecutionContext) extends ProjectFeedService {

  private def onlyNew(feed: List[OpenGraphObject], last: Option[Post]): List[OpenGraphObject] = {
    if (last.isEmpty) {
      return feed
    }
    val hasLast = feed.indexWhere(_.url == last.get.url)
    if (hasLast == -1) {
      feed
    } else {
      feed.take(hasLast)
    }
  }

  override def refresh(prj: Project): Future[List[Post]] = {
    if (prj.rss.isEmpty)
      return Future.successful(List.empty)

    for {
      feed <- rssFeedReader.read(prj.rss.get)
      lastPost <- postService.findLastByProject(prj._id)
      newPosts = onlyNew(feed, lastPost)
      created <- Future.sequence(newPosts.map(postEnrichService.enrich(prj, _).flatMap(postService.create)))
    } yield {
      created
    }
  }

}