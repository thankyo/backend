package com.clemble.thank.controller

import com.clemble.thank.service.repository.UserRepository
import com.clemble.thank.service.{UserPaymentService, UserService}
import com.clemble.thank.util.AuthEnv
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

@Singleton
case class UserPaymentController @Inject()(
                                            userService: UserRepository,
                                            paymentService: UserPaymentService,
                                            silhouette: Silhouette[AuthEnv],
                                            implicit val ec: ExecutionContext
) extends Controller {

  def myPayments() = silhouette.SecuredAction.async(implicit req => {
    val fPayments = for {
      user <- userService.findById(req.identity.id).map(_.get)
    } yield {
      paymentService.payments(user)
    }
    ControllerSafeUtils.okChunked(fPayments)
  })

}
