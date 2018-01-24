package com.clemble.loveit.thank.controller

import java.time.YearMonth
import javax.inject.Inject

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.{SupportedProjectService}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

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

}
