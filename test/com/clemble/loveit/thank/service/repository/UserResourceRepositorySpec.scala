package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.thank.model.UserResource
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json

@RunWith(classOf[JUnitRunner])
class UserResourceRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val repo = dependency[UserResourceRepository]

  "GET" in {
    val user = createUser()
    val expectedUserRes = Json.toJson(user).asOpt[UserResource]
    await(repo.find(user.id)) shouldEqual expectedUserRes
  }

}
