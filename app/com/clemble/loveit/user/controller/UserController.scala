package com.clemble.loveit.user.controller

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.common.model.User
import com.clemble.loveit.common.service.UserService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
case class UserController @Inject()(
  userService: UserService,
  silhouette: Silhouette[AuthEnv],
  components: ControllerComponents,
  implicit val ec: ExecutionContext
) extends LoveItController(silhouette, components) {

  def get(id: UserID) = silhouette.SecuredAction.async(implicit req => {
    val realId = idOrMe(id)
    val fUserOpt = userService.findById(realId)
    fUserOpt.map({
      case Some(user) if isMe(id) => Ok(user)
      case Some(user) => Ok(user.clean())
      case None => NotFound
    })
  })

  def updateMyProfile() = silhouette.SecuredAction.async(parse.json[User])(implicit req => {
    val user = req.body.copy(id = req.identity.id)
    userService.update(user).map(Ok(_))
  })

}
