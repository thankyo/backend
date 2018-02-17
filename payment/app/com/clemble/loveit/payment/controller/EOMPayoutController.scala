package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.repository.EOMPayoutRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
case class EOMPayoutController @Inject()(
                                          payoutRepo: EOMPayoutRepository,
                                          silhouette: Silhouette[AuthEnv],
                                          components: ControllerComponents
                                        )(
                                          implicit ec: ExecutionContext
                                        ) extends LoveItController(components) {

  def listMy() = silhouette.SecuredAction.async(req => {
    payoutRepo.findByUser(req.identity.id).map(Ok(_))
  })

}
