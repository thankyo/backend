package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.thank.model.UserResource
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UserResourceRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val repo = dependency[UserResourceRepository]

  "GET" in {
    val user = createUser()
    await(repo.find(user)) shouldNotEqual None
  }

  "Finds owner" in {
    val res = someRandom[Resource]
    val userResource = someRandom[UserResource].copy(owns = Set(res))

    await(repo.save(userResource)) shouldEqual true
    await(repo.findOwner(res)) shouldEqual Some(userResource.project)

    val childRes = Resource.from(s"${res.stringify()}/${someRandom[String]}")
    await(repo.findOwner(childRes)) shouldEqual Some(userResource.project)
  }

}
