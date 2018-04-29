package com.clemble.loveit.thank.controller

import java.util.UUID

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.model.{OwnedProject, Project, ProjectID, Resource, UserID}
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.common.model.OwnedProject
import com.clemble.loveit.thank.service._
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.{JsBoolean, JsObject, Json}
import play.api.mvc.ControllerComponents

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProjectController @Inject()(
  usrPrjService: UserProjectsService,
  service: ProjectService,
  feedService: ProjectFeedService,
  lookupService: ProjectLookupService,
  trackService: ProjectSupportTrackService,
  dibsOwnSvc: DibsProjectOwnershipService,
  googleOwnSvc: GoogleProjectOwnershipService,
  tumblrOwnSvc: TumblrProjectOwnershipService,
  silhouette: Silhouette[AuthEnv],
  components: ControllerComponents,
  implicit val ec: ExecutionContext
) extends LoveItController(components) {

  def getOwnedProjects() = silhouette.SecuredAction.async(implicit req => {
    usrPrjService.get(req.identity.id).map(userProjects => {
      Ok(userProjects)
    })
  })

  def deleteOwnedProject(url: Resource) = silhouette.SecuredAction.async(implicit req => {
    usrPrjService.deleteOwned(req.identity.id, url).map(Ok(_))
  })

  def getProjectsByUser(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    lookupService
      .findByUser(idOrMe(user))
      .map(Ok(_))
  })

  def updateProject(id: ProjectID) = silhouette.SecuredAction(parse.json[Project]).async(implicit req => {
    val user = req.identity.id
    val project = req.body.copy(user = user, _id = id)
    service.update(project).map(Ok(_))
  })

  def getProjectFeed(id: ProjectID) = silhouette.SecuredAction.async(implicit req => {
    val requester = req.identity.id
    lookupService.findById(id).flatMap {
      case Some(project) if project.user == requester =>
        feedService.refresh(project).map(Ok(_))
      case _ =>
        Future.successful(BadRequest)
    }
  })

  def getSupportedByUser(supporter: UserID) = silhouette.SecuredAction.async(implicit req => {
    trackService.
      getSupported(idOrMe(supporter)).
      map(projects => Ok(Json.toJson(projects)))
  })

  def getProject(project: ProjectID) = silhouette.UnsecuredAction.async(implicit req => {
    lookupService.findById(project).map {
      case Some(prj) => Ok(prj)
      case None => NotFound
    }
  })

  def dibsOnUrl() = silhouette.SecuredAction.async(parse.json[JsObject].map(json => (json \ "url").as[String]))(implicit req => {
    dibsOwnSvc.dibs(req.identity.id, req.body).map(Ok(_))
  })

  def verifyDibs(token: UUID) = silhouette.SecuredAction.async(implicit req => {
    dibsOwnSvc.verify(req.identity.id, token).map(Ok(_))
  })

  def reSendDibsVerification(url: Resource) = silhouette.SecuredAction.async(implicit req => {
    dibsOwnSvc.sendWHOISVerification(req.identity.id, url).map(email => Ok(JsBoolean(email.isDefined)))
  })

  def refreshGoogle() = silhouette.SecuredAction.async(implicit req => {
    googleOwnSvc.refresh(req.identity.id).map(Ok(_))
  })

  def refreshTumblr() = silhouette.SecuredAction.async(implicit req => {
    tumblrOwnSvc.refresh(req.identity.id).map(Ok(_))
  })

  def create() = silhouette.SecuredAction.async(parse.json[OwnedProject])(implicit req => {
    service.create(req.identity.id, req.body).map(Ok(_))
  })

  def delete(id: ProjectID) = silhouette.SecuredAction.async(implicit req => {
    service.delete(req.identity.id, id).map(if (_) Ok(JsBoolean(true)) else InternalServerError)
  })

}
