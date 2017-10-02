package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.repository.EOMPayoutRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class EOMPayoutController @Inject()(
                                          payoutRepo: EOMPayoutRepository,
                                          silhouette: Silhouette[AuthEnv],
                                          implicit val ec: ExecutionContext
                                        ) extends Controller {

  def listMy() = silhouette.SecuredAction(req => {
    val payouts = payoutRepo.findByUser(req.identity.id)
    Ok.chunked(payouts)
  })

}
