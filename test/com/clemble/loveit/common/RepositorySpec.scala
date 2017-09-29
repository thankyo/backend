package com.clemble.loveit.common

import com.clemble.loveit.payment.model.UserPayment
import com.clemble.loveit.payment.service.repository.UserPaymentRepository
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.repository.UserRepository

trait RepositorySpec extends ThankSpecification {

  lazy val userRepo = dependency[UserRepository]
  lazy val payRepo = dependency[UserPaymentRepository]

  def createUser() = {
    val user = someRandom[User]
    await(userRepo.save(someRandom[User]))
    await(payRepo.save(UserPayment from user))
    user
  }

}
