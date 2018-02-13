package com.clemble.loveit.thank.service

import javax.inject.Inject

import com.clemble.loveit.thank.model.{Post, Project}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

trait ProjectFeedService {

  def refresh(project: Project): Future[List[Post]]

}

object ProjectFeedService {

  def readRSS(project: Project, feed: Elem): List[Post] = List.empty

}

case class SimpleProjectFeedService @Inject()(wsClient: WSClient, postService: PostService, implicit val ec: ExecutionContext) extends ProjectFeedService {

  private def fetch(project: Project): Future[List[Post]] = {
    project.rss match {
      case Some(feedUrl) =>
        wsClient.url(feedUrl).get().map(feed => ProjectFeedService.readRSS(project, feed.xml))
      case None =>
        Future.successful(List.empty)
    }
  }

  override def refresh(project: Project): Future[List[Post]] = {
    fetch(project)
  }

}