package com.clemble.loveit.user.controller

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.service.SubscriptionManager
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsBoolean, JsDefined, JsObject, JsString}
import play.api.mvc.ControllerComponents

import scala.concurrent.{ExecutionContext, Future}

@deprecated
@Singleton
case class SubscriptionController @Inject()(
  subManager: SubscriptionManager,
  components: ControllerComponents,
  silhouette: Silhouette[AuthEnv],
  implicit val ec: ExecutionContext
) extends LoveItController(silhouette, components) {

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
