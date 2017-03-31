package com.clemble.loveit.controller

import com.clemble.loveit.model.ResourceOwnership
import com.clemble.loveit.service.UserService
import com.clemble.loveit.util.AuthEnv
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
    val fOwnership = userService.assignOwnership(req.identity.id, req.body)
    fOwnership.map(Created(_))
  })

}