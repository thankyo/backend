package com.clemble.thank.controller

import com.clemble.thank.model.ResourceOwnership
import com.clemble.thank.service.UserService
import com.clemble.thank.util.AuthEnv
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class ResourceOwnershipController @Inject()(
                                             userService: UserService,
                                             silhouette: Silhouette[AuthEnv],
                                             implicit val ec: ExecutionContext
                                           ) extends Controller {

  def assignOwnership() = silhouette.SecuredAction.async(parse.json[ResourceOwnership])(implicit req => {
    val fOwnership = userService.assignOwnership(req.identity.id, req.body.normalize())
    ControllerSafeUtils.created(fOwnership)
  })

}