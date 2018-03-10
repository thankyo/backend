package com.clemble.loveit.thank.service

import javax.inject.Inject

import com.clemble.loveit.thank.model.{OpenGraphImage, OpenGraphObject, Post, Project}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, XML}

trait ProjectFeedService {

  def refresh(project: Project): Future[List[Post]]

}

object ProjectFeedService {

  def readRSS(feed: Elem): List[OpenGraphObject] = FeedParser.parse(feed).toList

}

case class SimpleProjectFeedService @Inject()(wsClient: WSClient, postRefreshService: PostEnrichService, postService: PostService, implicit val ec: ExecutionContext) extends ProjectFeedService {

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

  override def refresh(project: Project): Future[List[Post]] = {
    for {
      fetched <- fetch(project)
      enriched <- Future.sequence(fetched.map(ogObj => postRefreshService.enrich(ogObj)))
      validPosts = enriched.filter(_.url.contains(project.url)) // TODO should be startsWith check
      created <- Future.sequence(validPosts.map(postService.create))
    } yield {
      created
    }
  }

}