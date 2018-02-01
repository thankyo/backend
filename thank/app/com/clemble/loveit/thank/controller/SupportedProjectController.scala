package com.clemble.loveit.thank.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.SupportedProjectService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.{ControllerComponents}

import scala.concurrent.ExecutionContext
import com.clemble.loveit.common.controller.LoveItController

@Singleton
class SupportedProjectController @Inject()(
                                            supportedProjectsService: SupportedProjectService,
                                            silhouette: Silhouette[AuthEnv],
                                            components: ControllerComponents,
                                            implicit val ec: ExecutionContext
                                              ) extends LoveItController(components) {

  def getMy() = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id
    supportedProjectsService
      .findProjectsByUser(user)
      .map(Ok(_))
  })

  def getSupported(supporter: UserID) = silhouette.SecuredAction.async(implicit req => {
    supportedProjectsService.
      getSupported(idOrMe(supporter)).
      map(projects => Ok(Json.toJson(projects)))
  })

}
