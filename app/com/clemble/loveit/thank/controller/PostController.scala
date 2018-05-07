package com.clemble.loveit.thank.controller

import javax.inject.Inject
import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.model.PostID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.PostService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.JsBoolean
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

class PostController @Inject()(
  service: PostService,
  silhouette: Silhouette[AuthEnv],
  components: ControllerComponents)(
  implicit val ec: ExecutionContext
) extends LoveItController(silhouette, components) {


  def get(id: PostID) = silhouette.SecuredAction.async(req => {
    service.findById(id) map {
      case Some(post) => Ok(post)
      case None => NotFound
    }
  })

  def delete(id: PostID) = silhouette.SecuredAction.async(req => {
    service.delete(id) map(if (_) Ok(JsBoolean(true)) else InternalServerError)
  })

  def refresh(id: PostID) = silhouette.SecuredAction.async(req => {
    service.refresh(req.identity.id, id).map(Ok(_))
  })

}
