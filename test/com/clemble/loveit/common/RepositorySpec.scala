package com.clemble.loveit.common

import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.UserPayment
import com.clemble.loveit.payment.service.repository.UserPaymentRepository
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.SupportedProjectRepository
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.repository.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global

trait RepositorySpec extends FunctionalThankSpecification {

  lazy val userRepo: UserRepository = dependency[UserRepository]
  lazy val prjRepo: SupportedProjectRepository = dependency[SupportedProjectRepository]
  lazy val payRepo: UserPaymentRepository = dependency[UserPaymentRepository]

  override def createUser(register: RegisterRequest = someRandom[RegisterRequest]): UserID = {
    val fUserID = for {
      user <- userRepo.save(register.toUser())
      _ <- payRepo.save(UserPayment from user)
    } yield {
      user.id
    }

    await(fUserID)
  }

  override def createProject(user: UserID = createUser(), resource: Resource = someRandom[Resource]): SupportedProject = {
    val project = SupportedProject(resource, user)
    await(prjRepo.saveProject(project))
    project
  }

  override def getUser(user: UserID): Option[User] = {
    await(userRepo.findById(user))
  }

}
