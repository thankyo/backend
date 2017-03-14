package com.clemble.thank.controller

import com.clemble.thank.service.ThankService
import com.clemble.thank.util.AuthEnv
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

case class ThankController @Inject()(
                                      service: ThankService,
                                      silhouette: Silhouette[AuthEnv],
                                      implicit val ec: ExecutionContext
                                    ) extends Controller {

  def get(url: String) = silhouette.UnsecuredAction.async(implicit req => {
    val fThank = service.findAll(url)
    ControllerSafeUtils.ok(fThank)
  })

  def thank(uri: String) = silhouette.SecuredAction.async(implicit req => {
    val giver = req.identity
    val fThank = service.thank(giver.id, uri)
    ControllerSafeUtils.ok(fThank)
  })

}
