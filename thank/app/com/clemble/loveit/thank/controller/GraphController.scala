package com.clemble.loveit.thank.controller

import javax.inject.Inject

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.model.{Post, SupportedProject}
import com.clemble.loveit.thank.service.PostService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

class GraphController @Inject()(
                                 service: PostService,
                                 silhouette: Silhouette[AuthEnv],
                                 components: ControllerComponents)(
                                 implicit val ec: ExecutionContext
                               ) extends LoveItController(components) {

  def get(res: Resource) = silhouette.UnsecuredAction.async(implicit req => {
    service
      .getOrCreate(res)
      .recover({ case _ => Post(res, SupportedProject.empty)})
      .map(Ok(_))
  })

}

