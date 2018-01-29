package com.clemble.loveit.thank.controller

import javax.inject.Inject

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.model.{Resource, Tag, UserID}
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.model.{OpenGraphObject, Post, SupportedProject}
import com.clemble.loveit.thank.service.PostService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

class GraphController @Inject()(
                                 service: PostService,
                                 silhouette: Silhouette[AuthEnv],
                                 components: ControllerComponents)(
                                 implicit val ec: ExecutionContext
                               ) extends LoveItController(components) {

  def get(res: Resource) = silhouette.UnsecuredAction.async(implicit req => {
    service
      .getOrCreate(res)
      .recover({ case _ => Post.from(res, SupportedProject.empty) })
      .map(Ok(_))
  })

  // TODO need a good security here
  def create() = silhouette.UnsecuredAction.async(parse.json[OpenGraphObject])(implicit req => {
    service
      .create(req.body)
      .map(Ok(_))
  })

  def searchByTags() = silhouette.SecuredAction.async(implicit req => {
    val tags: Set[Tag] = req.queryString.get("tags").map(_.toSet).getOrElse(Set.empty[Tag])
    service.findByTags(tags).map(posts => Ok(posts))
  })

  def searchByAuthor(author: UserID) = silhouette.SecuredAction.async(implicit req => {
    service.findByAuthor(author).map(posts => Ok(posts))
  })

}

