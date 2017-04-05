package com.clemble.loveit.thank.controller

import com.clemble.loveit.thank.model.ResourceOwnership
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.ResourceOwnershipService
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
class ResourceOwnershipController @Inject()(
                                             service: ResourceOwnershipService,
                                             silhouette: Silhouette[AuthEnv],
                                             implicit val ec: ExecutionContext
                                           ) extends Controller {

  def listMyOwnership() = silhouette.SecuredAction(implicit req => {
    val owned = service.listMy(req.identity.id)
    Ok.chunked(owned)
  })

  def assignOwnership() = silhouette.SecuredAction.async(parse.json[ResourceOwnership])(implicit req => {
    val fOwnership = service.assign(req.identity.id, req.body)
    fOwnership.map(Created(_))
  })

}