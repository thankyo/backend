package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.{CookieUtils, LoveItController}
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.ContributionStatisticsService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
case class StatisticsController @Inject()(
  contributionStatistics: ContributionStatisticsService,
  components: ControllerComponents,
  silhouette: Silhouette[AuthEnv]
  )
  (
    implicit val ec: ExecutionContext,
    implicit val cookieUtils: CookieUtils
  ) extends LoveItController(silhouette, components) {

  def getContributions(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    contributionStatistics.find(idOrMe(user)).map(Ok(_))
  })

}
