package com.clemble.loveit.thank.service

import javax.inject.Inject

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.thank.model.{OpenGraphObject, Post, Project}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, XML}

trait ProjectFeedService {

  def refresh(project: Project): Future[List[Post]]

}

object ProjectFeedService {

  def readRSS(project: Project, feed: Elem): List[Post] = {
    val items = feed \ "channel" \ "item"
    val posts = items.map(item => {
      Option((item \ "link").text).map(link =>
          Post(
            resource = Resource.from(link),
            project,
            ogObj = OpenGraphObject(
              url = link,
              title = Option((item \ "title").text)
            )
          )
      )
    })
    posts.flatten.toList
  }

}

case class SimpleProjectFeedService @Inject()(wsClient: WSClient, postService: PostService, implicit val ec: ExecutionContext) extends ProjectFeedService {

  private def fetch(project: Project): Future[List[Post]] = {
    project.rss match {
      case Some(feedUrl) =>
        wsClient.url(feedUrl).get().map(feed => ProjectFeedService.readRSS(project, XML.loadString(feed.body)))
      case None =>
        Future.successful(List.empty)
    }
  }

  override def refresh(project: Project): Future[List[Post]] = {
    fetch(project)
  }

}