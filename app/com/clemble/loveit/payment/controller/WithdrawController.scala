package com.clemble.loveit.payment.controller

import com.clemble.loveit.payment.service.PaymentService
import com.clemble.loveit.common.util.{AuthEnv}
import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.JsObject
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
class WithdrawController @Inject()(
                                    service: PaymentService,
                                    silhouette: Silhouette[AuthEnv],
                                    implicit val ec: ExecutionContext
                                  )extends Controller {

  def withdraw() = silhouette.SecuredAction.async(parse.json[JsObject].map(_ \ "amount"))(req => {
    val amount = req.body.as[Int]
    val fTransaction = service.withdraw(req.identity.id, amount)
    fTransaction.map(Ok(_))
  })


}
