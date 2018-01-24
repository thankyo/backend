package com.clemble.loveit.thank.controller

import javax.inject.Inject

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.model.{Money, Tag, UserID}
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.SupportedProjectService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc.{ControllerComponents, Request}

import scala.concurrent.{ExecutionContext, Future}

case class UserTagController @Inject()(
                                        prjService: SupportedProjectService,
                                        silhouette: Silhouette[AuthEnv],
                                        components: ControllerComponents,
                                        implicit val ec: ExecutionContext
                                      ) extends LoveItController(components) {

  def getUserTags(creator: UserID) = silhouette.SecuredAction.async(implicit req => {
    prjService
      .getProject(idOrMe(creator))
      .map(_ match {
        case Some(proj) => Ok(Json.toJson(proj.tags))
        case None => NotFound
      })
  })

  // TODO this is very non optimal
  def setMyTags() = silhouette.SecuredAction.async(implicit req => {
    val tags = req.body.asRaw.flatMap(_.asBytes()).map(bytes => bytes.utf8String).flatMap(str => Json.parse(str).asOpt[Set[Tag]])
    if (tags.isEmpty) {
      throw new IllegalArgumentException("Can't read tags in request")
    }

    for {
      saved <- prjService.assignTags(req.identity.id, tags.get) if (saved)
      currentTags <- prjService.getProject(req.identity.id).map(_.map(_.tags))
    } yield {
      currentTags.map(tags => Ok(Json.toJson(tags))).getOrElse(NotFound)
    }
  })

}
