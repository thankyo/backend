package com.clemble.loveit.thank.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{ProjectID, UserID}
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.{ROService, SupportedProjectService}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext
import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.thank.model.SupportedProject

@Singleton
class SupportedProjectController @Inject()(
                                            service: SupportedProjectService,
                                            roService: ROService,
                                            silhouette: Silhouette[AuthEnv],
                                            components: ControllerComponents,
                                            implicit val ec: ExecutionContext
                                              ) extends LoveItController(components) {

  def getMy() = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id
    service
      .findProjectsByUser(user)
      .map(Ok(_))
  })

  def listPending() = silhouette.SecuredAction.async(implicit req => {
    roService.list(req.identity.id).map(Ok(_))
  })

  def getUserProject(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    service
      .findProjectsByUser(idOrMe(user))
      .map(Ok(_))
  })

  def updateProject(id: ProjectID) = silhouette.SecuredAction(parse.json[SupportedProject]).async(implicit req => {
    val user = req.identity.id
    val project = req.body.copy(user = user, _id = id)
    service.update(project).map(Ok(_))
  })

  def getSupported(supporter: UserID) = silhouette.SecuredAction.async(implicit req => {
    service.
      getSupported(idOrMe(supporter)).
      map(projects => Ok(Json.toJson(projects)))
  })

  def getProject(project: ProjectID) = silhouette.SecuredAction.async(implicit req => {
    service.findById(project).map(_ match {
      case Some(prj) => Ok(prj)
      case None => NotFound
    })
  })

}
