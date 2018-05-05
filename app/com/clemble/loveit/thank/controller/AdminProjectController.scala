package com.clemble.loveit.thank.controller

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.AdminProjectService
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

case class AdminProjectController @Inject()(
  prjSvc: AdminProjectService,
  silhouette: Silhouette[AuthEnv],
  components: ControllerComponents)(
  implicit val ec: ExecutionContext
) extends LoveItController(components) {

  def listProjects () = silhouette.UnsecuredAction.async(implicit req => {
    prjSvc.findAll().map(Ok(_))
  })

}
