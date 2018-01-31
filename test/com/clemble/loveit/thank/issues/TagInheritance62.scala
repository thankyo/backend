package com.clemble.loveit.thank.issues

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.thank.model.{OpenGraphObject, Post, SupportedProject}
import com.clemble.loveit.thank.service._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

trait TagInheritance62 extends ServiceSpec with TagTestService with PostTestService with SupportedProjectTestService{

  "Tags inherited from project Author" in {
    // Step 1. Create scene
    val owner = createUser()

    // Step 2. Assigning tags
    val projectTags = Set("manga", "quote", "inspiration")
    assignTags(owner, projectTags)
    getTags(owner) shouldEqual projectTags

    // Step 3. Creating owned resource
    val resource = someRandom[Resource]
    assignOwnership(owner, resource)

    getTags(resource) shouldEqual projectTags

    // Step 4. Creating post under owned resource
    val objRes = someChildResource(resource)
    val ogo = someRandom[OpenGraphObject].copy(url = objRes.stringify(), tags = Set("comics"))
    createPost(ogo)

    getTags(objRes) shouldEqual projectTags ++ Set("comics")
  }

}

@RunWith(classOf[JUnitRunner])
class InternalTagInheritance62 extends TagInheritance62
  with InternalTagTestService
  with InternalPostTestService
  with InternalSupportedProjectTestService {
}
