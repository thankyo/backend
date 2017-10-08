package com.clemble.loveit.user.controller

import com.clemble.loveit.user.service.UserService
import com.clemble.loveit.common.util.AuthEnv
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.ControllerUtils._
import com.clemble.loveit.common.model.UserID
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
case class UserController @Inject()(
                                     userService: UserService,
                                     silhouette: Silhouette[AuthEnv],
                                     components: ControllerComponents,
                                     implicit val ec: ExecutionContext
                                   ) extends AbstractController(components) {

  def get(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    import com.clemble.loveit.user.model.User.userWriteable
    val realId = idOrMe(user)
    val fUserOpt = userService.findById(realId)
    fUserOpt.map(userOpt => Ok(userOpt.get))
  })

}
