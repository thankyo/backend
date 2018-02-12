package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.SupportedProject
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SupportedProjectRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val trackRepo = dependency[SupportTrackRepository]

  def assignOwnership(user: UserID, res: Resource): Boolean = await(prjRepo.saveProject(SupportedProject(res, user)))

  def listOwned(user: UserID): List[Resource] = await(prjRepo.findProjectsByUser(user)).map(_.resource)

  def findOwner(res: Resource): Option[UserID] = await(prjRepo.findProject(res)).map(_.user)


  "Resource repo creates a project" in {
    val user = createUser()

    await(prjRepo.findProjectsByUser(user)) shouldNotEqual None
  }

  "Update repo" in {
    val giver = createUser()
    val owner = createUser()

    val project = someRandom[SupportedProject]

    await(trackRepo.markSupportedBy(giver, project)) shouldEqual true
    await(trackRepo.getSupported(giver)) shouldEqual List(project._id)
  }

  "LIST" should {

    "return empty on new user" in {
      val user = createUser()

      listOwned(user) shouldEqual List.empty
    }

    "return empty on non existent" in {
      val user = IDGenerator.generate()

      listOwned(user) shouldEqual List.empty
    }

  }

  "ASSIGN OWNERSHIP" should {

    "create ownership" in {
      val user = createUser()
      val res = someRandom[Resource]

      assignOwnership(user, res) shouldEqual true

      listOwned(user) shouldEqual List(res)
    }

    "ignore multiple assignments to the same user" in {
      val user = createUser()
      val res = someRandom[Resource]

      assignOwnership(user, res) shouldEqual true
      assignOwnership(user, res) shouldEqual true

      listOwned(user) shouldEqual List(res)
    }

    "override ownership" in {
      val A = createUser()
      val B = createUser()

      listOwned(A) shouldEqual List.empty[Resource]
      listOwned(B) shouldEqual List.empty[Resource]

      val res = someRandom[Resource]

      assignOwnership(A, res) shouldEqual true
      assignOwnership(B, res) shouldEqual true

      listOwned(A) shouldEqual List.empty[Resource]
      listOwned(B) shouldEqual List(res)
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

      val parentRes = someRandom[HttpResource]
      assignOwnership(owner, parentRes) shouldEqual true

      val childRes = HttpResource(s"${parentRes.uri}/${someRandom[Long]}")
      findOwner(childRes) shouldEqual Some(owner)
    }


  }


}
