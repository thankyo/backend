package com.clemble.loveit.payment.controller

import com.clemble.loveit.common.util.AuthEnv
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.payment.service.repository.EOMChargeRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class PaymentTransactionController @Inject()(
                                                   transactionRepo: EOMChargeRepository,
                                                   silhouette: Silhouette[AuthEnv],
                                                   implicit val ec: ExecutionContext
                                            ) extends Controller {

  def listMy() = silhouette.SecuredAction(req => {
    val thanks = transactionRepo.findByUser(req.identity.id)
    Ok.chunked(thanks)
  })

}
