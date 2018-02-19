package com.clemble.loveit.common

import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.UserPayment
import com.clemble.loveit.payment.service.repository.UserPaymentRepository
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.thank.service.repository.ProjectRepository
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.repository.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global

trait RepositorySpec extends FunctionalThankSpecification {

  lazy val userRepo: UserRepository = dependency[UserRepository]
  lazy val prjRepo: ProjectRepository = dependency[ProjectRepository]
  lazy val payRepo: UserPaymentRepository = dependency[UserPaymentRepository]

  override def createUser(register: RegistrationRequest = someRandom[RegistrationRequest]): UserID = {
    val fUserID = for {
      user <- userRepo.save(register.toUser())
      _ <- payRepo.save(UserPayment from user)
    } yield {
      user.id
    }

    await(fUserID)
  }

  override def createProject(user: UserID = createUser(), resource: Resource = someRandom[Resource]): Project = {
    val project = Project(resource, user)
    await(prjRepo.saveProject(project))
    project
  }

  override def getUser(user: UserID): Option[User] = {
    await(userRepo.findById(user))
  }

}
