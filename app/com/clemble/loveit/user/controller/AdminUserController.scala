package com.clemble.loveit.user.controller

import javax.inject.Inject

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.service.repository.UserRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.JsNumber
import play.api.mvc.{ControllerComponents}

import scala.concurrent.ExecutionContext

case class AdminUserController @Inject()(
                                          repo: UserRepository,
                                          components: ControllerComponents,
                                          silhouette: Silhouette[AuthEnv]
                                        )
                                        (
                                          implicit
                                            ec: ExecutionContext
                                        ) extends LoveItController(components) {

  def count() = silhouette.SecuredAction.async(implicit req => {
    repo.count().map(count => Ok(JsNumber(count)))
  })

}
