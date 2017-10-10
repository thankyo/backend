package com.clemble.loveit.thank.controller

import javax.inject.Inject

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.UserSupportedProjectsService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext
import com.clemble.loveit.common.controller.ControllerUtils._

class UserSupportedProjectsController @Inject()(
                                                supportedProjectsService: UserSupportedProjectsService,
                                                silhouette: Silhouette[AuthEnv],
                                                components: ControllerComponents,
                                                implicit val ec: ExecutionContext
                                              ) extends AbstractController(components) {

  def getSupported(supporter: UserID) = silhouette.SecuredAction.async(implicit req => {
    supportedProjectsService.
      getSupported(idOrMe(supporter)).
      map(projects => Ok(Json.toJson(projects)))
  })

}
