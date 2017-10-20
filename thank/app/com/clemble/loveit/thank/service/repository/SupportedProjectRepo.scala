package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.model.SupportedProject

import scala.concurrent.Future

trait SupportedProjectRepo {

  def markSupported(supporter: UserID, project: SupportedProject): Future[Boolean]

  def getSupported(supporter: UserID): Future[List[SupportedProject]]

}
