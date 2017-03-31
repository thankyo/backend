package com.clemble.loveit.controller

import com.clemble.loveit.model.Resource
import com.clemble.loveit.service.ThankService
import com.clemble.loveit.util.AuthEnv
import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

case class ThankController @Inject()(
                                      service: ThankService,
                                      silhouette: Silhouette[AuthEnv],
                                      implicit val ec: ExecutionContext
                                    ) extends Controller {

  def get(resource: Resource) = silhouette.UnsecuredAction.async(implicit req => {
    val fThank = service.getOrCreate(resource)
    fThank.map(Ok(_))
  })

  def thank(resource: Resource) = silhouette.SecuredAction.async(implicit req => {
    val giver = req.identity
    val fThank = service.thank(giver.id, resource)
    fThank.map(Ok(_))
  })

}
