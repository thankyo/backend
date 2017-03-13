package com.clemble.thank.controller

import com.clemble.thank.service.repository.UserRepository
import com.clemble.thank.service.{UserPaymentService, UserService}
import com.google.inject.{Inject, Singleton}
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

@Singleton
case class UserPaymentController @Inject()(
                                            userService: UserRepository,
                                            paymentService: UserPaymentService,
                                            implicit val ec: ExecutionContext
) extends Controller {

  def payments(id: String) = Action.async(req => {
    val fPayments = for {
      user <- userService.findById(id).map(_.get)
    } yield {
      paymentService.payments(user)
    }
    ControllerSafeUtils.okChunked(fPayments)
  })

}
