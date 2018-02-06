package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{ProjectID, UserID}
import com.clemble.loveit.thank.model.SupportedProject

import scala.concurrent.Future

trait SupportTrackRepository {

  def isSupportedBy(supporter: UserID, project: SupportedProject): Future[Boolean]

  def getSupported(supporter: UserID): Future[List[ProjectID]]

}
