package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.ResourceException
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.Project
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ProjectRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val trackRepo = dependency[ProjectSupportTrackRepository]

  def assignOwnership(user: UserID, url: Resource): Project = await(prjRepo.save(Project(url, user, url)))

  def listOwned(user: UserID): List[Resource] = await(prjRepo.findByUser(user)).map(_.url)

  def findOwner(url: Resource): Option[UserID] = await(prjRepo.findByUrl(url)).map(_.user)


  "Resource repo creates a project" in {
    val user = createUser()

    await(prjRepo.findByUser(user)) shouldNotEqual None
  }

  "Update repo" in {
    val giver = createUser()
    val owner = createUser()

    val project = someRandom[Project]

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
      val res = randomResource

      assignOwnership(user, res)

      listOwned(user) shouldEqual List(res)
    }

    "ignore multiple assignments to the same user" in {
      val user = createUser()
      val res = randomResource

      assignOwnership(user, res)
      assignOwnership(user, res) should throwA[ResourceException]

      listOwned(user) shouldEqual List(res)
    }

    "override ownership" in {
      val A = createUser()
      val B = createUser()

      listOwned(A) shouldEqual List.empty[Resource]
      listOwned(B) shouldEqual List.empty[Resource]

      val res = randomResource

      assignOwnership(A, res)
      assignOwnership(B, res) should throwA[ResourceException]

      listOwned(A) shouldEqual List(res)
      listOwned(B) shouldEqual List.empty[Resource]
    }

  }

  "FIND OWNERSHIP" should {

    "find exact owner" in {
      val owner = createUser()
      val res = randomResource

      assignOwnership(owner, res)

      findOwner(res) shouldEqual Some(owner)
    }

    "find parent owner" in {
      val owner = createUser()

      val parentRes = randomResource
      assignOwnership(owner, parentRes)

      val childRes = s"${parentRes}/${someRandom[Long]}"
      findOwner(childRes) shouldEqual Some(owner)
    }


  }


}
