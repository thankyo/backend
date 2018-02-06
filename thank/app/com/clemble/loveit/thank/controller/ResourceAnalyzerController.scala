package com.clemble.loveit.thank.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.{CookieUtils, LoveItController}
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.ResourceAnalyzerService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
case class ResourceAnalyzerController @Inject()(
                                                 service: ResourceAnalyzerService,
                                                 silhouette: Silhouette[AuthEnv],
                                                 components: ControllerComponents)(
                                                 implicit val ec: ExecutionContext,
                                                 cookieUtils: CookieUtils
                                               ) extends LoveItController(components) {

  def analyze(url: String) = silhouette.UnsecuredAction.async(implicit req => {
    service.analyze(url).map(_ match {
      case Some(technology) => Ok(technology)
      case None => NotFound
    })
  })

}
