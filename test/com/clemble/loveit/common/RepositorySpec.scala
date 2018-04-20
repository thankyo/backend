package com.clemble.loveit.common

import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.common.model.{Project, Resource, User, UserID, Verification}
import com.clemble.loveit.payment.model.UserPayment
import com.clemble.loveit.payment.service.repository.UserPaymentRepository
import com.clemble.loveit.thank.model.UserProjects
import com.clemble.loveit.thank.service.repository.{ProjectRepository, UserProjectsRepository}
import com.clemble.loveit.user.service.repository.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global

trait RepositorySpec extends FunctionalThankSpecification {

  lazy val userRepo: UserRepository = dependency[UserRepository]
  lazy val prjRepo: ProjectRepository = dependency[ProjectRepository]
  lazy val usrPrjRepo: UserProjectsRepository = dependency[UserProjectsRepository]
  lazy val payRepo: UserPaymentRepository = dependency[UserPaymentRepository]

  override def createUser(register: RegistrationRequest = someRandom[RegistrationRequest]): UserID = {
    val fUserID = for {
      user <- userRepo.save(register.toUser())
      _ <- payRepo.save(UserPayment from user)
      _ <- usrPrjRepo.save(UserProjects from user)
    } yield {
      user.id
    }

    await(fUserID)
  }

  override def createProject(user: UserID = createUser(), url: Resource = randomResource): Project = {
    await(prjRepo.saveProject(Project(url, user, someRandom[String], someRandom[String], someRandom[Verification])))
  }

  override def getUser(user: UserID): Option[User] = {
    await(userRepo.findById(user))
  }

}
