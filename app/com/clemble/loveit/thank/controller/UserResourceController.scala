package com.clemble.loveit.thank.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.ControllerUtils._
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.repository.UserResourceRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class UserResourceController @Inject()(
                                             repo: UserResourceRepository,
                                             silhouette: Silhouette[AuthEnv],
                                             implicit val ec: ExecutionContext
                                           ) extends Controller {

  def get(owner: UserID) = silhouette.SecuredAction.async(implicit req => {
    val id = idOrMe(owner)
    val fUserResource = repo.find(id)
    fUserResource.map(_ match {
      case Some(res) => Ok(res)
      case None => NotFound
    })
  })

}
