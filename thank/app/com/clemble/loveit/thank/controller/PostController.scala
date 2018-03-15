package com.clemble.loveit.thank.controller

import javax.inject.Inject

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.model.OpenGraphObject
import com.clemble.loveit.thank.service.PostService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

class PostController @Inject()(service: PostService,
  silhouette: Silhouette[AuthEnv],
  components: ControllerComponents)(
  implicit val ec: ExecutionContext
) extends LoveItController(components) {


  def get(id: String) = silhouette.SecuredAction.async(req => {
    service.findById(id) map {
      case Some(post) => Ok(post)
      case None => NotFound
    }
  })

}
