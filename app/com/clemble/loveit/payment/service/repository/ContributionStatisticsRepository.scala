package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.ContributionStatistics

import scala.concurrent.Future

trait ContributionStatisticsRepository {

  def find(user: UserID): Future[ContributionStatistics]

}
