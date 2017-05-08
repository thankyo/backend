package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.test.util.{OwnershipVerificationGenerator, UserGenerator}
import com.clemble.loveit.thank.model.Verified
import com.clemble.loveit.user.service.repository.UserRepository
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OwnershipVerificationRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  lazy val userRepo = dependency[UserRepository]
  lazy val ownershipRepo = dependency[OwnershipVerificationRepository]

  "SAVES" in {
    val user = await(userRepo.save(UserGenerator.generate()))
    val ownership = await(ownershipRepo.save(OwnershipVerificationGenerator.generate().copy(requester = user.id)))
    ownership shouldNotEqual None
    await(userRepo.findById(user.id)).map(_.ownRequests) shouldEqual Some(Set(ownership))
  }

  "UPDATE STATUS" in {
    val user = await(userRepo.save(UserGenerator.generate()))
    val ownership = await(ownershipRepo.save(OwnershipVerificationGenerator.generate().copy(requester = user.id)))
    val updated = await(ownershipRepo.update(ownership, Verified))

    updated shouldEqual true

    val expected = ownership.copy(status = Verified)
    await(userRepo.findById(user.id)).map(_.ownRequests) shouldEqual Some(Set(expected))
  }


}
