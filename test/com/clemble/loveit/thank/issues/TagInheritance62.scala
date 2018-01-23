package com.clemble.loveit.thank.issues

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.thank.model.{OpenGraphObject, Post, SupportedProject}
import com.clemble.loveit.thank.service.{PostTestService, SupportedProjectTestService, TagTestService}

trait TagInheritance62 extends ServiceSpec with TagTestService with PostTestService with SupportedProjectTestService{

  "Tags inherited from Supported project" in {
    // Step 1. Create scene
    val owner = createUser()
    val resource = someRandom[Resource]

    assignOwnership(owner, resource)

    // Step 2. Assigning tags
    val projectTags = someRandom[Set[Tag]]
    assignTags(owner, projectTags)

    // Step 2.1 Checking project tags are as assigned
    getTags(owner) shouldEqual projectTags

    // Step 3. Creating post
    val objRes = someChildResource(resource)
    val openGraphObject = someRandom[OpenGraphObject].copy(url = objRes.stringify())
    val post = createPost(openGraphObject)

    post.tags shouldEqual projectTags
  }

}
