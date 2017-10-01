package com.clemble.loveit.common

import com.clemble.loveit.auth.models.requests.SignUpRequest
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.UserPayment
import com.clemble.loveit.payment.service.repository.UserPaymentRepository
import com.clemble.loveit.thank.model.UserResource
import com.clemble.loveit.thank.service.repository.UserResourceRepository
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.repository.UserRepository
import scala.concurrent.ExecutionContext.Implicits.global

trait RepositorySpec extends FunctionalThankSpecification {

  lazy val userRepo = dependency[UserRepository]
  lazy val payRepo = dependency[UserPaymentRepository]
  lazy val resRepo = dependency[UserResourceRepository]

  override def createUser(signUp: SignUpRequest = someRandom[SignUpRequest]): UserID = {
    val fUserID = for {
      user <- userRepo.save(User from signUp)
      _ <- payRepo.save(UserPayment from user)
      _ <- resRepo.save(UserResource from user)
    } yield {
      user.id
    }

    await(fUserID)
  }

  override def getUser(user: UserID): Option[User] = {
    await(userRepo.findById(user))
  }

}
