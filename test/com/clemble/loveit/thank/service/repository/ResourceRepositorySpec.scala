package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.test.util.{ResourceGenerator, UserGenerator}
import com.clemble.loveit.user.service.repository.UserRepository
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner
import org.apache.commons.lang3.RandomStringUtils._

@RunWith(classOf[JUnitRunner])
class ResourceRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec  {

  lazy val userRepo = dependency[UserRepository]
  lazy val resRepo = dependency[ResourceRepository]

  def createUser() = await(userRepo.save(UserGenerator.generate()))

  def assignOwnership(user: UserID, res: Resource) = await(resRepo.assignOwnership(user, res))
  def listOwned(user: UserID) = await(resRepo.listOwned(user))
  def findOwner(res: Resource) = await(resRepo.findOwner(res))

  "LIST" should {

    "return empty on new user" in {
      val user = createUser().id

      listOwned(user) shouldEqual Set.empty
    }

    "return empty on non existent" in {
      val user = IDGenerator.generate()

      listOwned(user) shouldEqual Set.empty
    }

  }

  "ASSIGN OWNERSHIP" should {

    "create ownership" in {
      val user = createUser().id
      val res = ResourceGenerator.generate()

      assignOwnership(user, res) shouldEqual true

      listOwned(user) shouldEqual Set(res)
    }

    "ignore multiple assignments to the same user" in {
      val user = createUser().id
      val res = ResourceGenerator.generate()

      assignOwnership(user, res) shouldEqual true
      assignOwnership(user, res) shouldEqual true

      listOwned(user) shouldEqual Set(res)
    }

    "override ownership" in {
      val A = createUser().id
      val B = createUser().id
      val res = ResourceGenerator.generate()

      assignOwnership(A, res) shouldEqual true
      assignOwnership(B, res) shouldEqual true

      listOwned(A) shouldEqual Set.empty[Resource]
      listOwned(B) shouldEqual Set(res)
    }

  }

  "FIND OWNERSHIP" should {

    "find exact owner" in {
      val owner = createUser().id
      val res = ResourceGenerator.generate()

      assignOwnership(owner, res) shouldEqual true

      findOwner(res) shouldEqual Some(owner)
    }

    "find parent owner" in {
      val owner = createUser().id

      val parentUri = s"${randomNumeric(10)}.com/${randomNumeric(4)}/"
      val parentRes = HttpResource(parentUri)
      assignOwnership(owner, parentRes) shouldEqual true

      val childRes = HttpResource(s"${parentUri}/${randomNumeric(10)}")
      findOwner(childRes) shouldEqual Some(owner)
    }


  }

}
