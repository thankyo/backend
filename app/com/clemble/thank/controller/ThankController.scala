package com.clemble.thank.controller

import com.clemble.thank.service.ThankService
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

case class ThankController @Inject()(service: ThankService, implicit val ec: ExecutionContext) extends Controller {

  def get(url: String) = Action.async(req => {
    val fThank = service.get(url)
    fThank.map(thank => {
      Ok(Json.toJson(thank))
    }).recover({
      case t: Throwable => InternalServerError(t.getMessage())
    })
  })

  def thank(uri: String) = Action.async(req => {
    val user = req.queryString.get("user").flatMap(_.headOption).getOrElse("unknown")
    val fThank = service.thank(user, uri)
    fThank.map(thank => {
      Ok(Json.toJson(thank))
    }).recover({
      case t: Throwable => InternalServerError(t.getMessage())
    })
  })

}
