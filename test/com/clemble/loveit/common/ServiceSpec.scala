package com.clemble.loveit.common

import com.clemble.loveit.auth.controller.SocialAuthController
import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.service.UserPaymentService
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.{ROService}
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService
import com.clemble.loveit.user.service.repository.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global

trait ServiceSpec extends FunctionalThankSpecification {

  lazy val authController: SocialAuthController = dependency[SocialAuthController]
  lazy val userService: UserService = dependency[UserService]
  lazy val userPayService: UserPaymentService = dependency[UserPaymentService]
  lazy val userRep: UserRepository = dependency[UserRepository]
  lazy val roService = dependency[ROService]

  override def createUser(register: RegisterRequest = someRandom[RegisterRequest]): UserID = {
    val fUserID = for {
      user <- userService.create(register.toUser())
      _ <- userPayService.create(user)
    } yield {
      user.id
    }
    await(fUserID)
  }

  override def getUser(user: UserID): Option[User] = {
    await(userService.findById(user))
  }

  def createProject(user: UserID = createUser(), res: Resource = someRandom[Resource]): SupportedProject = {
    await(roService.validate(SupportedProject(res, user)))
  }

}
