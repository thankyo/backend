package com.clemble.loveit.thank.controller

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.thank.service.ThankService
import com.clemble.loveit.common.util.AuthEnv
import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
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
