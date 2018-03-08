package com.clemble.loveit.common

import com.clemble.loveit.auth.controller.SocialAuthController
import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.service.UserPaymentService
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.thank.service.repository.ProjectRepository
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService
import com.clemble.loveit.user.service.repository.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global

trait ServiceSpec extends FunctionalThankSpecification {

  lazy val authController: SocialAuthController = dependency[SocialAuthController]
  lazy val userService: UserService = dependency[UserService]
  lazy val userPayService: UserPaymentService = dependency[UserPaymentService]
  lazy val userRep: UserRepository = dependency[UserRepository]
  lazy val prjRepo = dependency[ProjectRepository]

  override def createUser(register: RegistrationRequest = someRandom[RegistrationRequest]): UserID = {
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

  def createProject(user: UserID = createUser(), url: Resource = randomResource): Project = {
    val project = Project(url, user)
    await(prjRepo.save(project))
    project
  }

}
