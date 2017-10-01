package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.{ROVerification, Verified}
import com.clemble.loveit.thank.service.ROVerificationGenerator
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ROVerificationRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  lazy val verificationRepo = dependency[ROVerificationRepository]
  lazy val verGen = dependency[ROVerificationGenerator]

  def createVerification(user: UserID) = await(verificationRepo.save(user, someRandom[ROVerification[Resource]]))

  def getVerification(user: UserID) = await(verificationRepo.get(user))

  "GETS" in {
    val user = createUser()

    val verification = createVerification(user)

    val read = getVerification(user)
    Some(verification) shouldEqual read
  }

  "SAVES" in {
    val user = createUser()

    val verification = createVerification(user)

    getVerification(user) shouldEqual Some(verification)
  }

  "SAVES multiple ignored" in {
    val A = createUser()
    val B = createUser()
    val req = someRandom[ROVerification[Resource]]

    await(verificationRepo.save(A, req)) shouldNotEqual None
    await(verificationRepo.save(B, req)) should throwA[RepositoryException]

    getVerification(A) shouldEqual Some(req)
    getVerification(B) shouldEqual None
  }

  "UPDATE STATUS" in {
    val user = createUser()
    val verif = await(verificationRepo.save(user, someRandom[ROVerification[Resource]]))
    val updated = await(verificationRepo.update(user, verif.resource, Verified))

    updated shouldEqual true

    val expected = verif.copy(status = Verified)
    getVerification(user) shouldEqual Some(expected)
  }

  "REMOVES" in {
    val user = createUser()
    val ownership = createVerification(user)
    val removed = await(verificationRepo.delete(user))
    removed shouldEqual true
    getVerification(user) shouldEqual None
  }


}
