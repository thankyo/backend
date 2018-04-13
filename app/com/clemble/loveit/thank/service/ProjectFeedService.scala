package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{OpenGraphObject, Post, Project}
import javax.inject.Inject
import com.clemble.loveit.common.model.{OpenGraphImage, Project}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, XML}

trait ProjectFeedService {

  def refresh(project: Project): Future[List[Post]]

}

object ProjectFeedService {

  def readFeed(xml: String): List[OpenGraphObject] = readFeed(XML.loadString(xml))

  def readFeed(feed: Elem): List[OpenGraphObject] = FeedParser.parse(feed).toList

}

case class DefaultProjectFeedService @Inject()(wsClient: WSClient, postEnrichService: PostEnrichService, postService: PostService, implicit val ec: ExecutionContext) extends ProjectFeedService {

  import ProjectFeedService._

  private def fetch(project: Project): Future[List[OpenGraphObject]] = {
    project.rss match {
      case Some(feedUrl) =>
        wsClient.url(feedUrl).get()
          .map(feed => readFeed(feed.body))
          .map(_.map(ogObj => if (ogObj.tags.isEmpty) ogObj.copy(tags = project.tags) else ogObj))
      case None =>
        Future.successful(List.empty)
    }
  }

  override def refresh(project: Project): Future[List[Post]] = {
    for {
      fetched <- fetch(project)
      enriched <- Future.sequence(fetched.map(ogObj => postEnrichService.enrich(ogObj)))
      validPosts = enriched.filter(_.url.contains(project.url)) // TODO should be startsWith check
      created <- Future.sequence(validPosts.map(postService.create))
    } yield {
      created
    }
  }

}

case class SimpleProjectFeedService @Inject()(wsClient: WSClient, postEnrichService: PostEnrichService, postService: PostService, implicit val ec: ExecutionContext) extends ProjectFeedService {

  import ProjectFeedService._

  private def fetch(project: Project): Future[List[OpenGraphObject]] = {
    project.rss match {
      case Some(feedUrl) =>
        wsClient.url(feedUrl).get()
          .map(feed => readFeed(feed.body))
          .map(_.map(ogObj => if (ogObj.tags.isEmpty) ogObj.copy(tags = project.tags) else ogObj))
      case None =>
        Future.successful(List.empty)
    }
  }

  override def refresh(project: Project): Future[List[Post]] = {
    for {
      fetched <- fetch(project)
      enriched <- Future.sequence(fetched.map(ogObj => postEnrichService.enrich(ogObj)))
      validPosts = enriched.filter(_.url.contains(project.url)) // TODO should be startsWith check
      created <- Future.sequence(validPosts.map(postService.create))
    } yield {
      created
    }
  }

}