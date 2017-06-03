package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.{ROVerification, Verified}
import com.clemble.loveit.thank.service.ROVerificationGenerator
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class ROVerificationRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  lazy val verificationRepo = dependency[ROVerificationRepository]
  lazy val verGen = dependency[ROVerificationGenerator]

  def createVerification(user: UserID) = await(verificationRepo.save(someRandom[ROVerification[Resource]].copy(requester = user)))

  def getVerification(user: UserID, res: Resource) = await(verificationRepo.get(user, res))

  def listVerifications(user: UserID) = await(verificationRepo.list(user))

  "GETS" in {
    val user = createUser().id

    val verification = createVerification(user)

    val read = getVerification(user, verification.resource)
    Some(verification) shouldEqual read
  }

  "SAVES" in {
    val user = createUser().id

    val verification = createVerification(user)

    listVerifications(user) shouldEqual Set(verification)
  }

  "SAVES the same" in {
    val A = createUser()
    val res = someRandom[Resource]

    await(verificationRepo.save(verGen.generate(A.id, res)))
    Try{ await(verificationRepo.save(verGen.generate(A.id, res))) }

    val pendingVerif = listVerifications(A.id)
    pendingVerif.size shouldEqual 1
  }

  "SAVES multiple ignored" in {
    val A = createUser()
    val B = createUser()
    val req = someRandom[ROVerification[Resource]].copy(requester = A.id)

    await(verificationRepo.save(req)) shouldNotEqual None
    await(verificationRepo.save(req.copy(requester = B.id))) should throwA[RepositoryException]

    await(verificationRepo.list(A.id)) shouldEqual Set(req)
    await(verificationRepo.list(B.id)) shouldEqual Set()
  }

  "UPDATE STATUS" in {
    val user = createUser()
    val verif = await(verificationRepo.save(someRandom[ROVerification[Resource]].copy(requester = user.id)))
    val updated = await(verificationRepo.update(user.id, verif.resource, Verified))

    updated shouldEqual true

    val expected = verif.copy(status = Verified)
    await(verificationRepo.list(user.id)) shouldEqual Set(expected)
  }

  "REMOVES" in {
    val user = createUser()
    val ownership = createVerification(user.id)
    val removed = await(verificationRepo.delete(user.id, ownership.resource))
    removed shouldEqual true
    await(verificationRepo.list(user.id)) shouldEqual Set.empty
  }


}
