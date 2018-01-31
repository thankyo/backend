package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.{Resource, Tag}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

trait TagTestServiceScenario extends ServiceSpec with TagTestService with PostTestService {

  "assign tags to user" in {
    val user = createUser()

    val resource = someRandom[Resource]
    val project = createProject(user, resource)

    val newTags = someRandom[Set[Tag]]
    newTags shouldNotEqual getProjectTags(resource)

    assignTags(project, newTags) shouldEqual true
    getProjectTags(resource) shouldEqual newTags
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