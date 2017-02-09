package com.clemble.thank.controller

import akka.stream.scaladsl.Source
import com.clemble.thank.service.{UserPaymentService, UserService}
import com.google.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.libs.streams.Streams
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

@Singleton
case class UserPaymentController @Inject()(userService: UserService, paymentService: UserPaymentService, implicit val ec: ExecutionContext) extends Controller {

  def payments(id: String) = Action.async(req => {
    val fRes = for {
      user <- userService.get(id).map(_.get)
    } yield {
      val paymentPublisher = Streams.enumeratorToPublisher(paymentService.payments(user))
      val paymentSource = Source.fromPublisher(paymentPublisher)
      val jsonPaymentSource = paymentSource.map(payment => Json.toJson(payment))
      Ok.chunked(jsonPaymentSource)
    }
    fRes.recover({
      case t: Throwable => InternalServerError(t.getMessage())
    })
  })

}
