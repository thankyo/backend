package com.clemble.loveit.payment.controller

import com.clemble.loveit.payment.service.PaymentTransactionService
import com.clemble.loveit.common.util.AuthEnv
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class PaymentTransactionController @Inject()(
                                                   service: PaymentTransactionService,
                                                   silhouette: Silhouette[AuthEnv],
                                                   implicit val ec: ExecutionContext
                                            )extends Controller {

  def listMy() = silhouette.SecuredAction(req => {
    val thanks = service.list(req.identity.id)
    Ok.chunked(thanks)
  })

}
