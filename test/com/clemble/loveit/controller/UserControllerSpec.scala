package com.clemble.loveit.controller

import com.clemble.loveit.user.model.User
import com.clemble.loveit.test.util.{CommonSocialProfileGenerator}
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UserControllerSpec(implicit ee: ExecutionEnv) extends ControllerSpec {

  "CREATE" should {

    "Support single create" in {
      val socialProfile = CommonSocialProfileGenerator.generate()
      val userAuth = createUser(socialProfile, 0)

      val savedUser = getMyUser()(userAuth)
      val expectedUser = (User from socialProfile).copy(id = savedUser.id, created = savedUser.created)
      savedUser must beEqualTo(expectedUser)
    }

    "Return same user on the same authentication" in {
      val socialProfile = CommonSocialProfileGenerator.generate()
      val firstAuth = createUser(socialProfile, 0)
      val firstUser = getMyUser()(firstAuth)

      val secondAuth = createUser(socialProfile, 0)
      val secondUser = getMyUser()(firstAuth)

      firstAuth shouldNotEqual secondAuth
      secondUser shouldEqual firstUser
    }

  }

}
