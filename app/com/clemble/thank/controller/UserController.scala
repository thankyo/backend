package com.clemble.thank.controller

import com.clemble.thank.service.UserService
import com.clemble.thank.util.AuthEnv
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class UserController @Inject()(
                                     userService: UserService,
                                     silhouette: Silhouette[AuthEnv],
                                     implicit val ec: ExecutionContext
                                   ) extends Controller {

  def getMy() = silhouette.SecuredAction.async(implicit req => {
    val realId = req.identity.id
    val fUserOpt = userService.findById(realId)
    fUserOpt.map(userOpt => Ok(userOpt.get))
  })

}
