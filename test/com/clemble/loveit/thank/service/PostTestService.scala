package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.thank.model.{OpenGraphObject, Post}

trait PostTestService extends ServiceSpec {

  def someChildResource(res: Resource): Resource = s"${res}/${someRandom[Int]}/${someRandom[Int]}"

  def createPost(resource: Resource = randomResource): Resource = {
    // Step 1. Create owner & resource to own
    val owner = createUser()
    val resource = randomResource
    // Step 2. Assign ownership to resource
    createProject(owner, resource)
    // Step 3. Create OGO with specific resource
    val ogo = someRandom[OpenGraphObject].copy(url = resource)
    createPost(ogo)
    // Step 4. Returning original resource
    resource
  }

  def createPost(ogo: OpenGraphObject): Post

}

trait InternalPostTestService extends ServiceSpec with PostTestService {

  lazy val postService = dependency[PostService]

  override def createPost(ogo: OpenGraphObject): Post = {
    await(postService.create(ogo))
  }

}
