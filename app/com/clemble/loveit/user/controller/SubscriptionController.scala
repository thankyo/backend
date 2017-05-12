package com.clemble.loveit.user.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.user.service.SubscriptionManager
import play.api.libs.json.{JsBoolean, JsDefined, JsObject, JsString}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionController @Inject() (subManager: SubscriptionManager, implicit val ec: ExecutionContext) extends Controller {

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
