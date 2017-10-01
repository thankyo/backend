package com.clemble.loveit.thank.service

import javax.inject.Inject

import akka.actor.Actor
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.service.repository.UserSupportedProjectsRepo
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService

import scala.concurrent.{ExecutionContext, Future}

trait UserSupportedProjectsService {

  def getSupported(user: UserID): Future[List[User]]

  def markSupported(supporter: UserID, project: UserID): Future[Boolean]

}

class SimpleUserSupportedProjectsService @Inject()(
                                                    userService: UserService,
                                                    repo: UserSupportedProjectsRepo,
                                                    implicit val ec: ExecutionContext
                                                  ) extends UserSupportedProjectsService {

  override def getSupported(user: UserID): Future[List[User]] = {
    repo.getSupported(user)
  }

  override def markSupported(supporter: UserID, project: UserID): Future[Boolean] = {
    for {
      projectOpt <- userService.findById(project) if (projectOpt.isDefined)
      updated <- repo.markSupported(supporter, projectOpt.get)
    } yield {
      updated
    }
  }

}
