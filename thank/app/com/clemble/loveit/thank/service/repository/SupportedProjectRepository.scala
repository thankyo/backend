package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Tag, UserID}
import com.clemble.loveit.thank.model.SupportedProject

import scala.concurrent.Future

trait SupportedProjectRepository {

  def getProject(userID: UserID): Future[Option[SupportedProject]]

  def assignTags(userID: UserID, tags: Set[Tag]): Future[Boolean]

}
