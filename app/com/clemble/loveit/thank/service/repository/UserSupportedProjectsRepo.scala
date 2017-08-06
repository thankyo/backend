package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.user.model.UserIdentity

import scala.concurrent.Future

trait UserSupportedProjectsRepo {

  def getRef(project: UserID): Future[Option[UserIdentity]]

  def markSupported(supporter: UserID, project: UserIdentity): Future[Boolean]

  def getSupported(supporter: UserID): Future[List[UserIdentity]]

}
