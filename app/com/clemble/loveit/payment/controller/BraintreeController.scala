package com.clemble.loveit.payment.controller

import java.util.Currency

import com.clemble.loveit.payment.model.{BraintreeRequest, Money}
import com.clemble.loveit.payment.service.BraintreeService
import com.clemble.loveit.util.{AuthEnv, LoveItCurrency}
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class BraintreeController @Inject()(
                                           braintreeService: BraintreeService,
                                           silhouette: Silhouette[AuthEnv],
                                           implicit val ec: ExecutionContext
                                ) extends Controller {

  def generateToken() = silhouette.SecuredAction.async(implicit req => {
    val tokenResp = braintreeService.generateToken().map(t => Ok(s""""${t}""""))
    tokenResp
  })

  def processNonce() = silhouette.SecuredAction.async(parse.json[BraintreeRequest])(implicit req => {
    val user = req.identity.id
    val transaction = braintreeService.process(user, req.body)
    transaction.map(Ok(_))
  })

}
