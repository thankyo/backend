package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.Tag
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

trait TagTestServiceScenario extends ServiceSpec with TagTestService with PostTestService {

  "assign tags to user" in {
    val user = createUser()

    val tags = getTags(user)
    val newTags = someRandom[Set[Tag]]
    tags shouldNotEqual newTags

    assignTags(user, newTags) shouldEqual true
    getTags(user) shouldEqual newTags
  }

  "assign tags to post" in {
    val post = createPost()

    val tags = getTags(post)
    val newTags = someRandom[Set[Tag]]

    assignTags(post, newTags) shouldEqual true
    getTags(post) shouldEqual newTags
  }

}

@RunWith(classOf[JUnitRunner])
class RepoTagTestServiceSpec extends TagTestServiceScenario with RepoTagTestService with InternalPostTestService {
}

@RunWith(classOf[JUnitRunner])
class InternalTagTestServiceSpec extends TagTestServiceScenario with InternalTagTestService with InternalPostTestService {
}