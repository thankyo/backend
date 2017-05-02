package com.clemble.loveit.payment.controller

import java.util.Currency

import com.clemble.loveit.payment.service.PaymentTransactionService
import com.clemble.loveit.user.service.UserService
import com.clemble.loveit.common.util.{AuthEnv, LoveItCurrency}
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.JsObject
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
class WithdrawController @Inject()(
                                    service: PaymentTransactionService,
                                    silhouette: Silhouette[AuthEnv],
                                    implicit val ec: ExecutionContext
                                  )extends Controller {

  def withdraw() = silhouette.SecuredAction.async(parse.json[JsObject])(req => {
    val amount = (req.body \ "amount").as[Int]
    val currency = LoveItCurrency.getInstance("USD")
    val fTransaction = service.withdraw(req.identity.id, amount, currency)
    fTransaction.map(Ok(_))
  })


}
