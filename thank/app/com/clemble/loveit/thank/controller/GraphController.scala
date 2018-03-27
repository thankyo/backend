package com.clemble.loveit.thank.controller

import javax.inject.Inject
import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.error.{PaymentException, SelfLovingForbiddenException}
import com.clemble.loveit.common.model.{Resource, Tag, UserID}
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.model.{OpenGraphObject, Post, Project}
import com.clemble.loveit.thank.service.PostService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.{JsBoolean, JsObject}
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

class GraphController @Inject()(
                                 service: PostService,
                                 silhouette: Silhouette[AuthEnv],
                                 components: ControllerComponents)(
                                 implicit val ec: ExecutionContext
                               ) extends LoveItController(components) {

  def get(url: Resource) = silhouette.UnsecuredAction.async(implicit req => {
    service
      .getPostOrProject(url)
      .recover({ case _ => Left(Post.from(url, Project.error(url))) })
      .map(_ match {
        case Left(post) => Ok(post)
        case Right(project) => Ok(Post.from(url, project))
      })
  })

  // TODO !!! BEFORE PRODUCTION need a good security here
  def create() = silhouette.UnsecuredAction.async(parse.json[OpenGraphObject])(implicit req => {
    service
      .create(req.body)
      .map(Ok(_))
  })

  def createMyPost() = silhouette.SecuredAction.async(parse.json[OpenGraphObject])(implicit req => {
    service
      .create(req.body)
      .map(Ok(_))
  })

  def searchByTags() = silhouette.SecuredAction.async(implicit req => {
    val tags: Set[Tag] = req.queryString.get("tags").map(_.toSet).getOrElse(Set.empty[Tag])
    service.findByTags(tags).map(posts => Ok(posts))
  })

  def searchByAuthor(author: UserID) = silhouette.SecuredAction.async(implicit req => {
    service.findByAuthor(idOrMe(author)).map(posts => Ok(posts))
  })

  def searchByProject(project: UserID) = silhouette.SecuredAction.async(implicit req => {
    service.findByProject(project).map(posts => Ok(posts))
  })

  def hasSupported(url: Resource) = silhouette.SecuredAction.async(implicit req => {
    service.hasSupported(req.identity.id, url).map(supported => Ok(JsBoolean(supported)))
  })

  def support = silhouette.SecuredAction.async(parse.json[JsObject].map(_ \ "url").map(_.as[Resource]))(implicit req => {
    service.thank(req.identity.id, req.body).map(Ok(_))
  })

}

