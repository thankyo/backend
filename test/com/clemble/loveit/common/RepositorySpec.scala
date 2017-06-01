package com.clemble.loveit.common

import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.repository.UserRepository

trait RepositorySpec extends ThankSpecification {

  lazy val userRepo = dependency[UserRepository]
  def createUser() = await(userRepo.save(someRandom[User]))

}
