package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.{Project, Resource, Tag}
import com.clemble.loveit.thank.service.repository.PostRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TagTestService {

  def assignTags(project: Project, tags: Set[Tag]): Boolean

  def getProjectTags(url: Resource): Set[Tag]

  def assignTags(url: Resource, tags: Set[Tag]): Boolean

  def getTags(url: Resource): Set[Tag]

}

trait RepoTagTestService extends TagTestService with ServiceSpec {

  lazy val postRepo = dependency[PostRepository]

  override def assignTags(project: Project, tags: Set[Tag]): Boolean = {
    await(prjRepo.update(project.copy(tags = tags)))
  }

  override def getProjectTags(project: Resource): Set[Tag] = {
    await(prjRepo.findByUrl(project)).map(_.tags).getOrElse(Set.empty[Tag])
  }

  override def assignTags(url: Resource, tags: Set[Tag]): Boolean = {
    val fPostUpdated = postService.getPostOrProject(url).flatMap({
      case Left(post) =>
        postService.create(post.ogObj.copy(tags = tags)).map(_ => true)
      case Right(_) =>
        Future.successful(false)
    })

    await(fPostUpdated)
  }

  def getTags(url: Resource): Set[Tag] = {
    await(postRepo.findByResource(url).map(_.map(_.ogObj.tags).getOrElse(Set.empty[Tag])))
  }

}

trait InternalTagTestService extends TagTestService with ServiceSpec {

  lazy val lookupService = dependency[ProjectLookupService]

  override def assignTags(project: Project, tags: Set[Tag]): Boolean = {
    await(prjService.update(project.copy(tags = tags)).map(_ => true))
  }

  override def assignTags(url: Resource, tags: Set[Tag]): Boolean = {
    val fPostUpdated = postService.getPostOrProject(url).flatMap({
      case Left(post) =>
        postService.create(post.ogObj.copy(tags = tags)).map(_ => true)
      case Right(_) =>
        Future.successful(false)
    })

    await(fPostUpdated)
  }

  override def getProjectTags(url: Resource): Set[Tag] = {
    await(lookupService.findByUrl(url)).map(_.tags).getOrElse(Set.empty[String])
  }

  override def getTags(res: Resource): Set[Tag] = {
    await(postService.getPostOrProject(res)) match {
      case Left(post) => post.ogObj.tags
      case Right(project) => project.tags
    }
  }
}