package com.clemble.thank.controller

import com.clemble.thank.service.{BraintreeService, UserService}
import com.clemble.thank.util.AuthEnv
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
    ControllerSafeUtils.ok(braintreeService.generateToken())
  })

  def processNounce() = silhouette.SecuredAction(implicit req => {
    Ok("232")
  })

}
