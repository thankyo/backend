package com.clemble.loveit.thank.controller

import com.clemble.loveit.user.service.UserService
import com.clemble.loveit.thank.model.ResourceOwnership
import com.clemble.loveit.common.util.AuthEnv
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
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