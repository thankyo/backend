package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{DibsProject, EmailProject, OwnedProject, Project}
import com.clemble.loveit.thank.model.UserProject
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class UserProjectRepositorySpec extends RepositorySpec {

  lazy val repo: UserProjectRepository = dependency[UserProjectRepository]

  "Save" in {
    val projects = someRandom[UserProject]

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

  "Update dibs on project" in {
    val user = createUser()

    val dibs = 1 to 10 map(i => someRandom[DibsProject].copy(verified = false))
    await(repo.saveDibsProjects(user, dibs))

    val projectToValidate = dibs(Random.nextInt(8)).url

    val afterValidation = await(repo.validateDibsProject(user, projectToValidate))

    val validatedProject = afterValidation.dibs.find(_.url == projectToValidate)
    validatedProject shouldNotEqual None
    validatedProject.get.verified shouldEqual true
  }

  "Update email on project" in {
    val user = createUser()

    val dibs = 1 to 10 map(_ => someRandom[EmailProject].copy(verified = false))
    await(repo.saveEmailProjects(user, dibs))

    val projectToValidate = dibs(Random.nextInt(8)).email

    val afterValidation = await(repo.validateEmailProject(user, projectToValidate))

    val validatedProject = afterValidation.email.find(_.email == projectToValidate)
    validatedProject shouldNotEqual None
    validatedProject.get.verified shouldEqual true
  }

}
