package com.clemble.loveit.common

import com.clemble.loveit.auth.controllers.SocialAuthController
import com.clemble.loveit.auth.models.requests.SignUpRequest
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.service.UserPaymentService
import com.clemble.loveit.thank.service.{ROService, UserResourceService}
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService
import com.clemble.loveit.user.service.repository.UserRepository
import scala.concurrent.ExecutionContext.Implicits.global

trait ServiceSpec extends FunctionalThankSpecification {

  lazy val authController = dependency[SocialAuthController]
  lazy val userService = dependency[UserService]
  lazy val userPayService = dependency[UserPaymentService]
  lazy val userResService = dependency[UserResourceService]
  lazy val userRep = dependency[UserRepository]

  lazy val roService = dependency[ROService]

  override def createUser(signUp: SignUpRequest = someRandom[SignUpRequest]): UserID = {
    val fUserID = for {
      user <- userService.save(User from signUp)
      _ <- userPayService.createAndSave(user)
      _ <- userResService.createAndSave(user)
    } yield {
      user.id
    }
    await(fUserID)
  }

  def assignOwnership(user: UserID, res: Resource) = await(roService.assignOwnership(user, res))

}
