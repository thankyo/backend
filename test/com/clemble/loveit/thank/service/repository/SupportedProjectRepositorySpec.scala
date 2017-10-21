package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.thank.model.{SupportedProject}
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SupportedProjectRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val repo = dependency[SupportedProjectRepository]

  "Resource repo creates a project" in {
    val user = createUser()

    await(repo.getProject(user)) shouldNotEqual None
  }

  "Update repo" in {
    val giver = createUser()
    val owner = createUser()

    val project = SupportedProject from getUser(owner).get

    await(repo.markSupported(giver, project)) shouldEqual true
    await(repo.getSupported(giver)) shouldEqual List(project)
  }

}
