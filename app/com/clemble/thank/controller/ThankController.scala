package com.clemble.thank.controller

import com.clemble.thank.service.ThankService
import com.google.inject.Inject
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

case class ThankController @Inject()(service: ThankService, implicit val ec: ExecutionContext) extends Controller {

  def get(url: String) = Action.async(req => {
    val fThank = service.get(url)
    ControllerSafeUtils.ok(fThank)
  })

  def thank(uri: String) = Action.async(req => {
    val user = req.queryString.get("user").flatMap(_.headOption).getOrElse("unknown")
    val fThank = service.thank(user, uri)
    ControllerSafeUtils.ok(fThank)
  })

}
