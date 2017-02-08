package com.clemble.thank.controller

import com.clemble.thank.model.{User, UserId}
import com.clemble.thank.service.UserService
import com.google.inject.Singleton
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

@Singleton
case class UserController(service: UserService) extends Controller {

  def create() = Action.async(request => {
    val userOpt = request.body.asJson.flatMap(_.asOpt[User])
    val fSavedUser = userOpt.map(service.create).getOrElse(Future.failed(new IllegalArgumentException("Invalid User format")))
    fSavedUser.map({
      Ok(_)
    }).recover({
      case t: Throwable => InternalServerError(t.getMessage)
    })
  })

  def get(id: UserId) = Action.async({
    val fUserOpt = service.get(id)
    fUserOpt.map(_ match {
      case Some(user) => Ok(user)
      case None => NotFound()
    }).recover({
      case t: Throwable => InternalServerError(t.getMessage())
    })
  })

}
