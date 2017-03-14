package com.clemble.thank.controller

import akka.util.ByteString
import com.clemble.thank.model.Payment
import com.clemble.thank.service.repository.UserRepository
import com.clemble.thank.service.{UserPaymentService, UserService}
import com.clemble.thank.util.AuthEnv
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.http.Writeable
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

@Singleton
case class UserPaymentController @Inject()(
                                            userService: UserRepository,
                                            paymentService: UserPaymentService,
                                            silhouette: Silhouette[AuthEnv],
                                            implicit val ec: ExecutionContext
) extends Controller {

  def paymentToJson = (payment: Payment) => {
    val json = Payment.jsonFormat.writes(payment)
    ByteString(json.toString())
  }

  implicit val paymentWriteable = new Writeable[Payment](paymentToJson, Some(JSON))

  def myPayments() = silhouette.SecuredAction(implicit req => {
    val payments = paymentService.payments(req.identity.id)
    ControllerSafeUtils.ok(payments)
  })

}
