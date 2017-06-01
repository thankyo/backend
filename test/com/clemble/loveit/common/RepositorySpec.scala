package com.clemble.loveit.common

import com.clemble.loveit.test.util.UserGenerator
import com.clemble.loveit.user.service.repository.UserRepository

trait RepositorySpec extends ThankSpecification {

  lazy val userRepo = dependency[UserRepository]
  def createUser() = await(userRepo.save(UserGenerator.generate()))

}
