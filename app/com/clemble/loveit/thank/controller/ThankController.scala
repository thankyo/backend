package com.clemble.loveit.thank.controller

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.thank.service.ThankService
import com.clemble.loveit.common.util.AuthEnv
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.CookieUtils
import com.clemble.loveit.common.error.ResourceException
import com.clemble.loveit.common.error.ResourceException._
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{Controller, Result}

import com.clemble.loveit.thank.controller.html.hasNotThanked
import com.clemble.loveit.thank.controller.html.ownerMissing
import com.clemble.loveit.thank.controller.html.hasThanked

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class ThankController @Inject()(
                                      service: ThankService,
                                      silhouette: Silhouette[AuthEnv],
                                      implicit val ec: ExecutionContext
                                    ) extends Controller {

  private def getJson(res: Resource): Future[Result] = {
    service.getOrCreate(res).map(Ok(_))
  }

  private def getHtml(giver: Option[String], res: Resource): Future[Result] = {
    val fResponse = giver match {
      case Some(giver) =>
        for {
          thanked <- service.thanked(giver, res)
        } yield {
          if (thanked) {
            Ok(hasThanked())
          } else {
            Ok(hasNotThanked(res))
          }
        }
      case None =>
        Future.successful(Ok(hasNotThanked(res)))
    }
    fResponse.recover({
      case ResourceException(OWNER_MISSING_CODE, _) =>
        Ok(ownerMissing(res))
    })
  }

  def get(res: Resource) = silhouette.UnsecuredAction.async(implicit req => {
    render.async({
      case Accepts.Json => getJson(res)
      case Accepts.Html => getHtml(CookieUtils.readUser(req), res)
    })
  })

  def thank(resource: Resource) = silhouette.SecuredAction.async(implicit req => {
    val giver = req.identity
    val fThank = service.thank(giver.id, resource)
    fThank.map(Ok(_))
  })

}
