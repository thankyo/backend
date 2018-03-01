package com.clemble.loveit.thank.issues

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.thank.model.{OpenGraphObject, Post, Project}
import com.clemble.loveit.thank.service._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

trait TagInheritance62 extends ServiceSpec with TagTestService with PostTestService with ProjectTestService{

  "Tags inherited from parent project" in {
    // Step 1. Create scene
    val owner = createUser()

    // Step 2. Creating owned resource
    val url = randomResource
    val project = createProject(owner, url)

    // Step 3. Assigning tags
    val projectTags = Set("manga", "quote", "inspiration")
    assignTags(project, projectTags)
    getProjectTags(url) shouldEqual projectTags

    getTags(url) shouldEqual projectTags

    // Step 4. Creating post under owned resource
    val childUrl = someChildResource(url)
    val ogo = someRandom[OpenGraphObject].copy(url = childUrl, tags = Set("comics"))
    createPost(ogo)

    getTags(childUrl) shouldEqual projectTags ++ Set("comics")
  }

}

@RunWith(classOf[JUnitRunner])
class InternalTagInheritance62 extends TagInheritance62
  with InternalTagTestService
  with InternalPostTestService
  with InternalProjectTestService {
}
