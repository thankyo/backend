package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{UserID}
import com.clemble.loveit.thank.model.{SupportedProject}
import com.clemble.loveit.user.service.UserService

import scala.concurrent.{ExecutionContext, Future}


trait OwnedProjectService {

  def list(user: UserID): Future[List[SupportedProject]]

  def enable(prj: SupportedProject): Future[SupportedProject]

  def refresh(user: UserID): Future[List[SupportedProject]]

}

@Singleton
case class SimpleOwnedProjectService @Inject()(
                                      supportedProjectService: SupportedProjectService,
                                      postService: PostService,
                                      refreshService: OwnedProjectRefreshService,
                                      userService: UserService,
                                      implicit val ec: ExecutionContext
                                    ) extends OwnedProjectService {

  override def refresh(user: UserID): Future[List[SupportedProject]] = {
    refreshService.fetch(user)
  }

  override def list(user: UserID): Future[List[SupportedProject]] = {
    refresh(user)
  }

  override def enable(project: SupportedProject): Future[SupportedProject] = {
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
