package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{DibsProject, OwnedProject, Project}
import com.clemble.loveit.thank.model.UserProjects
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UserProjectRepositorySpec extends RepositorySpec {

  lazy val repo: UserProjectsRepository = dependency[UserProjectsRepository]

  "Save" in {
    val projects = someRandom[UserProjects]

    await(repo.save(projects)) shouldEqual projects
    await(repo.save(projects)) should throwA[Throwable]
  }

  "Update Owned Project" in {
    val user = createUser()

    val dibsProject = someRandom[DibsProject]
    await(repo.saveDibsProjects(user, Seq(dibsProject)))

    await(repo.findById(user)).get.dibs should containAllOf(Seq(dibsProject))
  }

  "Update Owned Project with additional details" in {
    val user = createUser()

    val dibsProject = someRandom[DibsProject]
    await(repo.saveDibsProjects(user, Seq(dibsProject)))

    val modifiedProject = dibsProject.copy(title = "Another title")
    await(repo.saveDibsProjects(user, Seq(modifiedProject)))

    await(repo.findById(user)).get.dibs shouldEqual Seq(modifiedProject)
  }

  "Delete Owned Project" in {
    val user = createUser()

    val dibsProject = someRandom[DibsProject]
    await(repo.saveDibsProjects(user, Seq(dibsProject)))
    await(repo.deleteDibsProject(user, dibsProject.url))

    await(repo.findById(user)).get.dibs should not(containAllOf(Seq(dibsProject)))
    await(repo.findById(user)).get.dibs shouldEqual List.empty
  }

}
