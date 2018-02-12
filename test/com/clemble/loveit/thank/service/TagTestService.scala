package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.{Resource, Tag}
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.thank.service.repository.{PostRepository, ProjectRepository}

import scala.concurrent.ExecutionContext.Implicits.global

trait TagTestService {

  def assignTags(project: Project, tags: Set[Tag]): Boolean

  def getProjectTags(resource: Resource): Set[Tag]

  def assignTags(res: Resource, tags: Set[Tag]): Boolean

  def getTags(res: Resource): Set[Tag]

}

trait RepoTagTestService extends TagTestService with ServiceSpec {

  private val prjRepo = dependency[ProjectRepository]
  private val postRepo = dependency[PostRepository]

  override def assignTags(project: Project, tags: Set[Tag]): Boolean = {
    await(prjRepo.assignTags(project.resource, tags))
  }

  override def getProjectTags(project: Resource): Set[Tag] = {
    return await(prjRepo.findProject(project)).map(_.tags).getOrElse(Set.empty[Tag])
  }

  override def assignTags(res: Resource, tags: Set[Tag]): Boolean = {
    await(postRepo.assignTags(res, tags))
  }

  def getTags(res: Resource): Set[Tag] = {
    await(postRepo.findByResource(res).map(_.map(_.ogObj.tags).getOrElse(Set.empty[Tag])))
  }

}

trait InternalTagTestService extends TagTestService with ServiceSpec {

  private val prjService = dependency[ProjectService]
  private val postService = dependency[PostService]

  override def assignTags(project: Project, tags: Set[Tag]): Boolean = {
    await(prjService.update(project.copy(tags = tags)).map(_ => true))
  }

  override def assignTags(res: Resource, tags: Set[Tag]): Boolean = {
    await(postService.assignTags(res, tags))
  }

  override def getProjectTags(resource: Resource): Set[Tag] = {
    await(prjService.findProject(resource)).map(_.tags).getOrElse(Set.empty[String])
  }

  override def getTags(res: Resource): Set[Tag] = {
    await(postService.getPostOrProject(res)) match {
      case Left(post) => post.ogObj.tags
      case Right(project) => project.tags
    }
  }
}