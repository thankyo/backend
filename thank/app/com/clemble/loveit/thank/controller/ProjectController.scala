package com.clemble.loveit.thank.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{ProjectID, UserID}
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.{ProjectFeedService, ProjectService, ProjectSupportTrackService}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents

import scala.concurrent.{ExecutionContext, Future}
import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.thank.model.Project

@Singleton
class ProjectController @Inject()(
                                   service: ProjectService,
                                   feedService: ProjectFeedService,
                                   trackService: ProjectSupportTrackService,
                                   silhouette: Silhouette[AuthEnv],
                                   components: ControllerComponents,
                                   implicit val ec: ExecutionContext
                                              ) extends LoveItController(components) {

  def getOwnedProjects() = silhouette.SecuredAction.async(implicit req => {
    service.findOwned(req.identity.id).map(Ok(_))
  })

  def getProjectsByUser(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    service
      .findProjectsByUser(idOrMe(user))
      .map(Ok(_))
  })

  def updateProject(id: ProjectID) = silhouette.SecuredAction(parse.json[Project]).async(implicit req => {
    val user = req.identity.id
    val project = req.body.copy(user = user, _id = id)
    service.update(project).map(Ok(_))
  })

  def getProjectFeed(id: ProjectID) = silhouette.SecuredAction.async(implicit req => {
    val requester = req.identity.id
    service.findById(id).flatMap(_ match {
      case Some(project) if (project.user == requester) =>
        feedService.refresh(project).map(Ok(_))
      case _ =>
        Future.successful(BadRequest)
    })
  })

  def refreshMyProjects = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id
    service.refresh(user).map(Ok(_))
  })

  def getSupportedByUser(supporter: UserID) = silhouette.SecuredAction.async(implicit req => {
    trackService.
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
