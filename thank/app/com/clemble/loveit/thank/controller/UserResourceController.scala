package com.clemble.loveit.thank.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.UserResourceService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
case class UserResourceController @Inject()(
                                             service: UserResourceService,
                                             silhouette: Silhouette[AuthEnv],
                                             components: ControllerComponents,
                                             implicit val ec: ExecutionContext
                                           ) extends LoveItController(components) {

  def get(owner: UserID) = silhouette.SecuredAction.async(implicit req => {
    val id = idOrMe(owner)
    val fUserResource = service.find(id)
    fUserResource.map(_ match {
      case Some(res) => Ok(res)
      case None => NotFound
    })
  })

}
