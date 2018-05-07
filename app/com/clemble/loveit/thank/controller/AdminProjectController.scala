package com.clemble.loveit.thank.controller

import com.clemble.loveit.auth.AdminAuthEnv
import com.clemble.loveit.common.controller.{AdminLoveItController}
import com.clemble.loveit.thank.service.AdminProjectService
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

case class AdminProjectController @Inject()(
  prjSvc: AdminProjectService,
  silhouette: Silhouette[AdminAuthEnv],
  components: ControllerComponents)(
  implicit val ec: ExecutionContext
) extends AdminLoveItController(silhouette, components) {

  def listProjects () = silhouette.SecuredAction.async(implicit req => {
    prjSvc.findAll().map(Ok(_))
  })

}
