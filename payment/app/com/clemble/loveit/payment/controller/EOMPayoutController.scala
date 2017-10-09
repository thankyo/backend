package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.repository.EOMPayoutRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
case class EOMPayoutController @Inject()(
                                          payoutRepo: EOMPayoutRepository,
                                          silhouette: Silhouette[AuthEnv],
                                          components: ControllerComponents
                                        )(
                                          implicit ec: ExecutionContext
                                        ) extends AbstractController(components) {

  def listMy() = silhouette.SecuredAction(req => {
    val payouts = payoutRepo.findByUser(req.identity.id)
    Ok.chunked(payouts)
  })

}
