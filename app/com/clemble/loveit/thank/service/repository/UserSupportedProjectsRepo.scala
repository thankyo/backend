package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.user.model.{User}

import scala.concurrent.Future

trait UserSupportedProjectsRepo {

  def getRef(resOwner: UserID): Future[Option[User]]

  def markSupported(supporter: UserID, project: User): Future[Boolean]

  def getSupported(supporter: UserID): Future[List[User]]

}
