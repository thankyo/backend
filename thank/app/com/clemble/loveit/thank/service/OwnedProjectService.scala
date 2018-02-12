package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{UserID}
import com.clemble.loveit.thank.model.{Project}
import com.clemble.loveit.user.service.UserService

import scala.concurrent.{ExecutionContext, Future}


trait OwnedProjectService {

  def enable(prj: Project): Future[Project]

  def refresh(user: UserID): Future[List[Project]]

}

@Singleton
case class SimpleOwnedProjectService @Inject()(
                                                supportedProjectService: ProjectService,
                                                postService: PostService,
                                                refreshService: OwnedProjectRefreshService,
                                                userService: UserService,
                                                implicit val ec: ExecutionContext
                                    ) extends OwnedProjectService {

  override def refresh(user: UserID): Future[List[Project]] = {
    refreshService.fetch(user)
  }

  override def enable(project: Project): Future[Project] = {
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
