package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{ProjectID, UserID}
import com.clemble.loveit.thank.model.Project

import scala.concurrent.Future

trait ProjectSupportTrackRepository {

  def markSupportedBy(supporter: UserID, project: Project): Future[Boolean]

  def getSupported(supporter: UserID): Future[List[ProjectID]]

}
