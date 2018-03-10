package com.clemble.loveit.payment.service

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.ContributionStatistics
import com.clemble.loveit.payment.service.repository.ContributionStatisticsRepository

import scala.concurrent.Future

trait ContributionStatisticsService {

  def find(user: UserID): Future[ContributionStatistics]

}


case class SimpleContributionStatisticsService(repo: ContributionStatisticsRepository) extends ContributionStatisticsService {

  override def find(user: UserID): Future[ContributionStatistics] = repo.find(user)

}