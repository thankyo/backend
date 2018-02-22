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

  def readRSS(feed: Elem): List[OpenGraphObject] = {
    val items = feed \ "channel" \ "item"
    val ogObj = items.map(item => {
      Option((item \ "link").text).map(link =>
        OpenGraphObject(
          url = link,
          title = Option((item \ "title").text),
          image = Option(item \ "thumbnail").filter(_.nonEmpty).flatMap(img => {
            val height = Option(img.\@("height")).filterNot(_.isEmpty).map(_.toInt)
            val width = Option(img.\@("width")).filterNot(_.isEmpty).map(_.toInt)
            Option(img.\@("url")).map(url => OpenGraphImage(url, height = height, width = width))
          })
        )
      )
    })
    ogObj.flatten.toList
  }

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
      validPosts = enriched.filter(_.url.contains(project.resource.uri)) // TODO should be startsWith check
      created <- Future.sequence(validPosts.map(postService.create))
    } yield {
      created
    }
  }

}