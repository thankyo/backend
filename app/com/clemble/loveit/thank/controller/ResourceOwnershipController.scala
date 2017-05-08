package com.clemble.loveit.thank.controller

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.{ResourceOwnershipService}
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
class ResourceOwnershipController @Inject()(
                                             service: ResourceOwnershipService,
                                             silhouette: Silhouette[AuthEnv],
                                             implicit val ec: ExecutionContext
                                           ) extends Controller {

  def listMyOwnership() = silhouette.SecuredAction.async(implicit req => {
    val fOwned = service.list(req.identity.id)
    fOwned.map(Ok(_))
  })

}