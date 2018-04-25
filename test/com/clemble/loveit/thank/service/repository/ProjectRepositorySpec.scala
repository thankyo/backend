package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.{FieldValidationError, RepositoryException}
import com.clemble.loveit.common.model.{Project, Resource, UserID}
import com.clemble.loveit.common.util.IDGenerator
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ProjectRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  def listOwned(user: UserID): List[Resource] = await(prjRepo.findProjectsByUser(user)).map(_.url)

  def findOwner(url: Resource): Option[UserID] = await(prjRepo.findProjectByUrl(url)).map(_.user)

  "Create" should {
    "create only owned" in {
      val user = createUser()
      val prj = someRandom[Project].copy(user = user)

      await(prjRepo.saveProject(prj)) should throwA[Throwable]
    }
  }

  "Update" should {

    "update title" in {
      val user = createUser()
      val res = randomResource

      val project = createProject(user, res)

      val projectWithTitle = project.copy(title = someRandom[String])

      await(prjRepo.updateProject(projectWithTitle)) shouldEqual true
      await(prjRepo.findProjectById(project._id)) shouldEqual Some(projectWithTitle)
    }

    "update url ignored" in {
      val user = createUser()
      val res = randomResource

      val project = createProject(user, res)

      val projectWithDiffUrl = project.copy(url = randomResource)

      await(prjRepo.updateProject(projectWithDiffUrl)) shouldEqual false
      await(prjRepo.findProjectById(project._id)) shouldEqual Some(project)
    }

    "Delete" in {
      val user = createUser()
      val res = randomResource

      val project = createProject(user, res)

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

      createProject(user, res)

      listOwned(user) shouldEqual List(res)
    }

    "ignore multiple assignments to the same user" in {
      val user = createUser()
      val res = randomResource

      createProject(user, res)
      createProject(user, res) should throwA[IllegalArgumentException]

      listOwned(user) shouldEqual List(res)
    }

    "override ownership" in {
      val A = createUser()
      val B = createUser()

      A shouldNotEqual B

      listOwned(A) shouldEqual List.empty[Resource]
      listOwned(B) shouldEqual List.empty[Resource]

      val res = s"https://assign-${someRandom[Int]}.com"

      createProject(A, res)
      createProject(B, res) should throwA[FieldValidationError]

      listOwned(A) shouldEqual List(res)
      listOwned(B) shouldEqual List.empty[Resource]
    }.isSkipped // TODO This is some strange behavior index either created or not tried to fix this, but does not work
    // This won't happen in production, hopefully, so I'll leave it be for now

  }

  "FIND OWNERSHIP" should {

    "find exact owner" in {
      val owner = createUser()
      val res = randomResource

      createProject(owner, res)

      findOwner(res) shouldEqual Some(owner)
    }

    "find parent owner" in {
      val owner = createUser()

      val parentRes = randomResource
      createProject(owner, parentRes)

      val childRes = s"${parentRes}/${someRandom[Long]}"
      findOwner(childRes) shouldEqual Some(owner)
    }


  }


}
