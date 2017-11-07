package com.clemble.loveit.user.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.user.service.SubscriptionManager
import play.api.libs.json.{JsBoolean, JsDefined, JsObject, JsString}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SubscriptionController @Inject()(
                                             subManager: SubscriptionManager,
                                             components: ControllerComponents,
                                             implicit val ec: ExecutionContext
                                           ) extends LoveItController(components) {

  def subscribeCreator() = Action.async(parse.json[JsObject].map(_ \ "email"))(implicit req => {
    req.body match {
      case JsDefined(JsString(email)) =>
        subManager.
          subscribeCreator(email).
          map(res => Ok(JsBoolean(res)))
      case _ =>
        Future.successful(NotFound)
    }
  })

  def subscribeContributor() = Action.async(parse.json[JsObject].map(_ \ "email"))(implicit req => {
    req.body match {
      case JsDefined(JsString(email)) =>
        subManager.
          subscribeContributor(email).
          map(res => Ok(JsBoolean(res)))
      case _ =>
        Future.successful(NotFound)
    }
  })

}
