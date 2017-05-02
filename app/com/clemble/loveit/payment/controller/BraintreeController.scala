package com.clemble.loveit.payment.controller

import com.clemble.loveit.payment.service.BraintreeProcessingService
import com.clemble.loveit.common.util.AuthEnv
import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
class BraintreeController @Inject()(
                                     braintreeService: BraintreeProcessingService,
                                     silhouette: Silhouette[AuthEnv],
                                     implicit val ec: ExecutionContext
                                ) extends Controller {

  def generateToken() = silhouette.SecuredAction.async(implicit req => {
    val tokenResp = braintreeService.generateToken().map(t => Ok(s""""${t}""""))
    tokenResp
  })

}
