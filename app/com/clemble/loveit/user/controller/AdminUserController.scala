package com.clemble.loveit.user.controller

import com.clemble.loveit.auth.AdminAuthEnv
import javax.inject.Inject
import com.clemble.loveit.common.controller.{AdminLoveItController}
import com.clemble.loveit.user.service.AdminUserService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.JsNumber
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

case class AdminUserController @Inject()(
                                          usrSvc: AdminUserService,
                                          components: ControllerComponents,
                                          silhouette: Silhouette[AdminAuthEnv]
                                        )
                                        (
                                          implicit ec: ExecutionContext
                                        ) extends AdminLoveItController(silhouette, components) {

  def count() = silhouette.SecuredAction.async(implicit req => {
    usrSvc.count().map(count => Ok(JsNumber(count)))
  })

  def list()= silhouette.SecuredAction.async(implicit req => {
    usrSvc.list().map(_.map(_.copy(email = "nonOf@your.business.yo"))).map(Ok(_))
  })

}
