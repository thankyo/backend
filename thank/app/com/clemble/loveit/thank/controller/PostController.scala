package com.clemble.loveit.thank.controller

import com.clemble.loveit.common.model.{Loved, NotLoved, OwnerMissing, ProjectID, Resource}
import com.clemble.loveit.thank.service.PostService
import com.clemble.loveit.common.util.AuthEnv
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.{CookieUtils, LoveItController}
import com.clemble.loveit.common.error.ResourceException
import com.clemble.loveit.common.error.ResourceException._
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{ControllerComponents, Result}
import com.clemble.loveit.thank.controller.html.post
import com.clemble.loveit.thank.model.Post

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class PostController @Inject()(
                                      service: PostService,
                                      silhouette: Silhouette[AuthEnv],
                                      components: ControllerComponents)(
                                      implicit val ec: ExecutionContext,
                                      cookieUtils: CookieUtils
                                    ) extends LoveItController(components) {

  private def getJson(res: Resource): Future[Result] = {
    service.getPostOrProject(res).map(_ match {
      case Left(post) => Ok(post)
      case Right(project) => Ok(Post.from(res, project))
    })
  }

  private def getHtml(giverOpt: Option[String], res: Resource): Future[Result] = {
    val fStatus = giverOpt match {
      case Some(giver) =>
        (for {
          thanked <- service.hasSupported(giver, res)
        } yield {
          if (thanked) {
            Loved
          } else {
            NotLoved
          }
        }).recover({
          case ResourceException(OWNER_MISSING_CODE, _) => OwnerMissing
        })
      case None =>
        Future.successful(NotLoved)
    }

    fStatus.map(status => Ok(post(res, status)))
  }

  def get(res: Resource) = silhouette.UnsecuredAction.async(implicit req => {
    render.async({
      case Accepts.Json() => getJson(res)
      case Accepts.Html() => getHtml(cookieUtils.readUser(req), res)
    })
  })

  def thank(resource: Resource) = silhouette.SecuredAction.async(implicit req => {
    val giver = req.identity.id
    val fThank = service.thank(giver, resource)
    fThank.map(Ok(_))
  })

}
