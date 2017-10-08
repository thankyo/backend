package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.repository.EOMChargeRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class EOMChargeController @Inject()(
                                     chargeRepo: EOMChargeRepository,
                                     silhouette: Silhouette[AuthEnv],
                                     components: ControllerComponents,
                                     implicit val ec: ExecutionContext
                                   ) extends AbstractController(components) {

  def listMy() = silhouette.SecuredAction(req => {
    val charges = chargeRepo.findByUser(req.identity.id)
    Ok.chunked(charges)
  })

}