package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.{RepositoryException, ResourceException}
import com.clemble.loveit.common.model.{Project, Resource, UserID, Verification}
import com.clemble.loveit.common.util.IDGenerator
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ProjectRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  def assignOwnership(user: UserID, url: Resource): Project = {
    await(prjRepo.saveProject(Project(url, user, someRandom[String], someRandom[String], someRandom[Verification])))
  }

  def listOwned(user: UserID): List[Resource] = await(prjRepo.findProjectsByUser(user)).map(_.url)

  def findOwner(url: Resource): Option[UserID] = await(prjRepo.findProjectByUrl(url)).map(_.user)


  "Update" should {

    "update title" in {
      val user = createUser()
      val res = randomResource

      val project = assignOwnership(user, res)

      val projectWithTitle = project.copy(title = someRandom[String])

      await(prjRepo.updateProject(projectWithTitle)) shouldEqual true
      await(prjRepo.findProjectById(project._id)) shouldEqual Some(projectWithTitle)
    }

    "update url ignored" in {
      val user = createUser()
      val res = randomResource

      val project = assignOwnership(user, res)

      val projectWithDiffUrl = project.copy(url = randomResource)

      await(prjRepo.updateProject(projectWithDiffUrl)) shouldEqual false
      await(prjRepo.findProjectById(project._id)) shouldEqual Some(project)
    }

    "Delete" in {
      val user = createUser()
      val res = randomResource

      val project = assignOwnership(user, res)

      await(prjRepo.deleteProject(user, project._id)) shouldEqual true
    }

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
      assignOwnership(user, res) should throwA[IllegalArgumentException]

      listOwned(user) shouldEqual List(res)
    }

    "override ownership" in {
      val A = createUser()
      val B = createUser()

      listOwned(A) shouldEqual List.empty[Resource]
      listOwned(B) shouldEqual List.empty[Resource]

      val res = randomResource

      assignOwnership(A, res)
      assignOwnership(B, res) should throwA[RepositoryException]

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
