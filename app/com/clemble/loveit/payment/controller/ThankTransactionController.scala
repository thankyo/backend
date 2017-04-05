package com.clemble.loveit.payment.controller

import com.clemble.loveit.payment.service.ThankTransactionService
import com.clemble.loveit.common.util.{AuthEnv, WriteableUtils}
import com.clemble.loveit.payment.model.ThankTransaction
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class ThankTransactionController @Inject()(
                                                 transactionService: ThankTransactionService,
                                                 silhouette: Silhouette[AuthEnv],
                                                 implicit val ec: ExecutionContext
) extends Controller {

  def listMy() = silhouette.SecuredAction(implicit req => {
    val thanks = transactionService.list(req.identity.id)
    Ok.chunked(thanks)
  })

}
