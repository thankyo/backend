package com.clemble.loveit.payment.controller

import com.clemble.loveit.payment.service.PaymentService
import com.clemble.loveit.common.util.AuthEnv
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.payment.model.PaymentRequest
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.JsObject
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class PaymentController @Inject()(
                                    service: PaymentService,
                                    silhouette: Silhouette[AuthEnv],
                                    implicit val ec: ExecutionContext
                                  )extends Controller {

  def process() = silhouette.SecuredAction.async(parse.json[PaymentRequest])(implicit req => {
    val user = req.identity.id
    val transaction = service.receive(user, req.body)
    transaction.map(Ok(_))
  })

}
