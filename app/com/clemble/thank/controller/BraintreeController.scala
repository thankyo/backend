package com.clemble.thank.controller

import java.util.Currency

import com.clemble.thank.model.{Money, PaymentTransaction}
import com.clemble.thank.service.BraintreeService
import com.clemble.thank.util.AuthEnv
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class BraintreeController @Inject()(
                                           braintreeService: BraintreeService,
                                           silhouette: Silhouette[AuthEnv],
                                           implicit val ec: ExecutionContext
                                ) extends Controller {

  def generateToken() = silhouette.SecuredAction.async(implicit req => {
    val token = braintreeService.generateToken().map(t => s""""${t}"""")
    ControllerSafeUtils.ok(token)
  })

  def processNonce() = silhouette.SecuredAction.async(parse.json[JsObject])(implicit req => {
    val transaction = braintreeService.processNonce(req.identity.id, (req.body \ "nonce").as[String], Money(10, Currency.getInstance("USD")))
    ControllerSafeUtils.ok(transaction)
  })

}
