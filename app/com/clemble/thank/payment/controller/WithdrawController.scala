package com.clemble.thank.payment.controller

import java.util.Currency

import com.clemble.thank.payment.service.PaymentTransactionService
import com.clemble.thank.service.UserService
import com.clemble.thank.util.AuthEnv
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.JsObject
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class WithdrawController @Inject()(
                                    userService: UserService,
                                    service: PaymentTransactionService,
                                    silhouette: Silhouette[AuthEnv],
                                    implicit val ec: ExecutionContext
                                  )extends Controller {

  def withdraw() = silhouette.SecuredAction.async(parse.json[JsObject])(req => {
    for {
      bankDetails <- userService.findById(req.identity.id).map(_.get.bankDetails)
      transaction <- service.withdraw(req.identity.id, bankDetails, (req.body \ "amount").as[Int], Currency.getInstance("USD"))
    } yield {
      Ok(transaction)
    }
  })


}
