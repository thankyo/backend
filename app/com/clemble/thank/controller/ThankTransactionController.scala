package com.clemble.thank.controller

import akka.util.ByteString
import com.clemble.thank.model.{ThankTransaction}
import com.clemble.thank.service.ThankTransactionService
import com.clemble.thank.util.AuthEnv
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.http.Writeable
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class ThankTransactionController @Inject()(
                                                 transactionService: ThankTransactionService,
                                                 silhouette: Silhouette[AuthEnv],
                                                 implicit val ec: ExecutionContext
) extends Controller {

  implicit val transactionWriteable = new Writeable[ThankTransaction]((payment: ThankTransaction) => {
    val json = ThankTransaction.jsonFormat.writes(payment)
    ByteString(json.toString())
  }, Some(JSON))

  def listMyTransactions() = silhouette.SecuredAction(implicit req => {
    val payments = transactionService.list(req.identity.id)
    ControllerSafeUtils.ok(payments)
  })

}
