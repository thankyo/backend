package com.clemble.loveit.thank.controller

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.thank.service.ThankService
import com.clemble.loveit.common.util.AuthEnv
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.{ResourceException}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class ThankController @Inject()(
                                      service: ThankService,
                                      silhouette: Silhouette[AuthEnv],
                                      implicit val ec: ExecutionContext
                                    ) extends Controller {

  private def ownerMissing() = {

  }

  def get(resource: Resource) = silhouette.UnsecuredAction.async(implicit req => {
    val fThank = service.getOrCreate(resource)
    val fResponse = for {
      thank <- fThank
    } yield {
      render {
        case Accepts.Html() => Ok(com.clemble.loveit.thank.controller.html.get(thank))
        case Accepts.Json() => Ok(thank)
      }
    }
    fResponse.recover({
      case ResourceException(ResourceException.OWNER_MISSING_CODE, _) =>
        Ok(com.clemble.loveit.thank.controller.html.ownerMissing(resource))})
  })

  def thank(resource: Resource) = silhouette.SecuredAction.async(implicit req => {
    val giver = req.identity
    val fThank = service.thank(giver.id, resource)
    fThank.map(Ok(_))
  })

}
