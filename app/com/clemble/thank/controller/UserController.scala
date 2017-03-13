package com.clemble.thank.controller

import com.clemble.thank.model.{UserId}
import com.clemble.thank.service.repository.UserRepository
import com.clemble.thank.util.AuthEnv
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{Controller}

import scala.concurrent.{ExecutionContext}

@Singleton
case class UserController @Inject()(
                                     repository: UserRepository,
                                     silhouette: Silhouette[AuthEnv],
                                     implicit val ec: ExecutionContext
                                   ) extends Controller {

  def get(id: UserId) = silhouette.SecuredAction.async(implicit req => {
    val realId = AuthUtils.whoAmI(id)
    val fUserOpt = repository.findById(realId)
    ControllerSafeUtils.okOrNotFound(fUserOpt)
  })

}
