package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.thank.model.SupportedProject

import scala.concurrent.{ExecutionContext, Future}

trait ROService {

  def validate(prj: SupportedProject):  Future[SupportedProject]

}

@Singleton
case class SimpleROService @Inject()(supportedProjectService: SupportedProjectService, postService: PostService, implicit val ec: ExecutionContext) extends ROService {

  override def validate(project: SupportedProject): Future[SupportedProject] = {
    // TODO assign is internal operation, so it might not need to throw Exception,
    // since verification has already been done before
    for {
      created <- supportedProjectService.create(project)
      _ = if (!created) throw new IllegalArgumentException("Could not create project")
      updPosts <- postService.updateOwner(project) if (updPosts)
      _ = if (!updPosts) throw new IllegalArgumentException("Failed to update posts")
    } yield {
      if (!updPosts)
        throw new IllegalArgumentException("Can't assign ownership")
      project
    }
  }

}
