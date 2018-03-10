package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.thank.model.{OpenGraphObject, Post}

trait PostTestService extends ServiceSpec {

  def someChildResource(url: Resource): Resource = s"${url}/${someRandom[Int]}/${someRandom[Int]}"

  def createPost(url: Resource = randomResource): Resource = {
    // Step 1. Create owner & resource to own
    val owner = createUser()
    // Step 2. Assign ownership to resource
    createProject(owner, url)
    // Step 3. Create OGO with specific resource
    val ogo = someRandom[OpenGraphObject].copy(url = url)
    createPost(ogo)
    // Step 4. Returning original resource
    url
  }

  def createPost(ogo: OpenGraphObject): Post

}

trait InternalPostTestService extends ServiceSpec with PostTestService {

  override def createPost(ogo: OpenGraphObject): Post = {
    await(postService.create(ogo))
  }

}
