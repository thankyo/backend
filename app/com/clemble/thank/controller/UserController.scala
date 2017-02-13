package com.clemble.thank.controller

import com.clemble.thank.model.{User, UserId}
import com.clemble.thank.service.UserService
import com.google.inject.{Inject, Singleton}
import play.api.mvc.{Action, BodyParsers, Controller}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class UserController @Inject()(
                                     service: UserService,
                                     implicit val ec: ExecutionContext
                                   ) extends Controller {

  def create() = Action.async(BodyParsers.parse.json) { req => {
    val userOpt = req.body.asOpt[User]
    val fSavedUser = userOpt.map(service.create).getOrElse(Future.failed(new IllegalArgumentException("Invalid User format")))
    ControllerSafeUtils.created(fSavedUser)
  }
  }

  def get(id: UserId) = Action.async({
    val fUserOpt = service.get(id)
    ControllerSafeUtils.okOrNotFound(fUserOpt)
  })

}
