package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.{Resource, Tag, UserID}
import com.clemble.loveit.thank.service.repository.{PostRepository, SupportedProjectRepository}

import scala.concurrent.ExecutionContext.Implicits.global

trait TagTestService {

  def assignTags(user: UserID, tags: Set[Tag]): Boolean

  def assignTags(res: Resource, tags: Set[Tag]): Boolean

  def getTags(project: UserID): Set[Tag]

  def getTags(res: Resource): Set[Tag]

}

trait RepoTagTestService extends TagTestService with ServiceSpec {

  private val prjRepo = dependency[SupportedProjectRepository]
  private val postRepo = dependency[PostRepository]

  override def assignTags(project: UserID, tags: Set[Tag]): Boolean = {
    await(prjRepo.assignTags(project, tags))
  }

  override def getTags(project: UserID): Set[Tag] = {
    await(prjRepo.getProject(project).map(_.map(_.tags).getOrElse(Set.empty[Tag])))
  }

  override def assignTags(res: Resource, tags: Set[Tag]): Boolean = {
    await(postRepo.assignTags(res, tags))
  }

  def getTags(res: Resource): Set[Tag] = {
    await(postRepo.findByResource(res).map(_.map(_.tags).getOrElse(Set.empty[Tag])))
  }

}

trait InternalTagTestService extends TagTestService with ServiceSpec {

  private val prjService = dependency[SupportedProjectService]
  private val postService = dependency[PostService]

  override def assignTags(user: UserID, tags: Set[Tag]): Boolean = {
    await(prjService.assignTags(user, tags))
  }

  override def assignTags(res: Resource, tags: Set[Tag]): Boolean = {
    await(postService.assignTags(res, tags))
  }

  override def getTags(project: UserID): Set[Tag] = {
    await(prjService.getProject(project)).map(_.tags).getOrElse(Set.empty[String])
  }

  override def getTags(res: Resource): Set[Tag] = {
    await(postService.getPostOrProject(res)) match {
      case Left(post) => post.tags
      case Right(project) => project.tags
    }
  }
}