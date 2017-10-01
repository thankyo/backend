package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.common.util.IDGenerator
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner
import org.apache.commons.lang3.RandomStringUtils._

@RunWith(classOf[JUnitRunner])
class RORepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec  {

  lazy val resOwnRepo = dependency[RORepository]

  def assignOwnership(user: UserID, res: Resource) = await(resOwnRepo.assignOwnership(user, res))
  def listOwned(user: UserID) = await(resOwnRepo.listOwned(user))
  def findOwner(res: Resource) = await(resOwnRepo.findOwner(res))

  "LIST" should {

    "return empty on new user" in {
      val user = createUser()

      listOwned(user) shouldEqual Set.empty
    }

    "return empty on non existent" in {
      val user = IDGenerator.generate()

      listOwned(user) shouldEqual Set.empty
    }

  }

  "ASSIGN OWNERSHIP" should {

    "create ownership" in {
      val user = createUser()
      val res = someRandom[Resource]

      assignOwnership(user, res) shouldEqual true

      listOwned(user) shouldEqual Set(res)
    }

    "ignore multiple assignments to the same user" in {
      val user = createUser()
      val res = someRandom[Resource]

      assignOwnership(user, res) shouldEqual true
      assignOwnership(user, res) shouldEqual true

      listOwned(user) shouldEqual Set(res)
    }

    "override ownership" in {
      val A = createUser()
      val B = createUser()

      listOwned(A) shouldEqual Set.empty[Resource]
      listOwned(B) shouldEqual Set.empty[Resource]

      val res = someRandom[Resource]

      assignOwnership(A, res) shouldEqual true
      assignOwnership(B, res) shouldEqual true

      listOwned(A) shouldEqual Set.empty[Resource]
      listOwned(B) shouldEqual Set(res)
    }

  }

  "FIND OWNERSHIP" should {

    "find exact owner" in {
      val owner = createUser()
      val res = someRandom[Resource]

      assignOwnership(owner, res) shouldEqual true

      findOwner(res) shouldEqual Some(owner)
    }

    "find parent owner" in {
      val owner = createUser()

      val parentUri = s"${randomNumeric(10)}.com/${randomNumeric(4)}/"
      val parentRes = HttpResource(parentUri)
      assignOwnership(owner, parentRes) shouldEqual true

      val childRes = HttpResource(s"${parentUri}/${randomNumeric(10)}")
      findOwner(childRes) shouldEqual Some(owner)
    }


  }

}
