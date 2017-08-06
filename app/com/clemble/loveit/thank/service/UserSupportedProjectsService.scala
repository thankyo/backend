package com.clemble.loveit.thank.service

import javax.inject.Inject

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.service.repository.UserSupportedProjectsRepo
import com.clemble.loveit.user.model.UserIdentity

import scala.concurrent.{ExecutionContext, Future}

trait UserSupportedProjectsService {

  def getSupported(user: UserID): Future[List[UserIdentity]]

  def markSupported(supporter: UserID, project: UserID): Future[Boolean]

}

class SimpleUserSupportedProjectsService @Inject()(
                                                  repo: UserSupportedProjectsRepo,
                                                  implicit val ec: ExecutionContext
                                                  )extends UserSupportedProjectsService {

  override def getSupported(user: UserID): Future[List[UserIdentity]] = {
    repo.getSupported(user)
  }

  override def markSupported(supporter: UserID, project: UserID): Future[Boolean] = {
    for {
      projectOpt <- repo.getRef(project) if (projectOpt.isDefined)
      updated <- repo.markSupported(supporter, projectOpt.get)
    } yield {
      updated
    }
  }

}
