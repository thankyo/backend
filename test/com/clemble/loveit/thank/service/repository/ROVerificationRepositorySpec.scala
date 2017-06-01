package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.test.util.{ROVerificationGenerator}
import com.clemble.loveit.thank.model.{VerificationID, Verified}
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ROVerificationRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  lazy val verificationRepo = dependency[ROVerificationRepository]

  def createVerification(user: UserID) = await(verificationRepo.save(ROVerificationGenerator.generate().copy(requester = user)))

  def getVerification(user: UserID, verifId: VerificationID) = await(verificationRepo.get(user, verifId))

  def listVerifications(user: UserID) = await(verificationRepo.list(user))

  "GETS" in {
    val user = createUser().id

    val verification = createVerification(user)

    val read = getVerification(user, verification.id)
    Some(verification) shouldEqual read
  }

  "SAVES" in {
    val user = createUser().id

    val verification = createVerification(user)

    listVerifications(user) shouldEqual Set(verification)
  }

  "SAVES multiple ignored" in {
    val A = createUser()
    val B = createUser()
    val req = ROVerificationGenerator.generate().copy(requester = A.id)

    await(verificationRepo.save(req)) shouldNotEqual None
    await(verificationRepo.save(req.copy(requester = B.id))) should throwA[RepositoryException]

    await(verificationRepo.list(A.id)) shouldEqual Set(req)
    await(verificationRepo.list(B.id)) shouldEqual Set()
  }

  "UPDATE STATUS" in {
    val user = createUser()
    val ownership = await(verificationRepo.save(ROVerificationGenerator.generate().copy(requester = user.id)))
    val updated = await(verificationRepo.update(ownership, Verified))

    updated shouldEqual true

    val expected = ownership.copy(status = Verified)
    await(verificationRepo.list(user.id)) shouldEqual Set(expected)
  }

  "REMOVES" in {
    val user = createUser()
    val ownership = createVerification(user.id)
    val removed = await(verificationRepo.delete(user.id, ownership.id))
    removed shouldEqual true
    await(verificationRepo.list(user.id)) shouldEqual Set.empty
  }


}
