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

  lazy val authController: SocialAuthController = dependency[SocialAuthController]
  lazy val userService: UserService = dependency[UserService]
  lazy val userPayService: UserPaymentService = dependency[UserPaymentService]
  lazy val userResService: UserResourceService = dependency[UserResourceService]
  lazy val userRep: UserRepository = dependency[UserRepository]

  lazy val roService = dependency[ROService]

  override def createUser(signUp: SignUpRequest = someRandom[SignUpRequest]): UserID = {
    val fUserID = for {
      user <- userService.save(signUp.toUser())
      _ <- userPayService.createAndSave(user)
      _ <- userResService.createAndSave(user)
    } yield {
      user.id
    }
    await(fUserID)
  }

  override def getUser(user: UserID): Option[User] = {
    await(userService.findById(user))
  }

  def assignOwnership(user: UserID, res: Resource) = await(roService.assignOwnership(user, res))

}
