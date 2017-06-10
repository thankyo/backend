package com.clemble.loveit.user.controller

import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.service.repository.UserRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.JsNumber
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

case class AdminUserController @Inject() (repo: UserRepository, silhouette: Silhouette[AuthEnv], implicit val ec: ExecutionContext) extends Controller {

  def count() = silhouette.SecuredAction.async(implicit req => {
    repo.count().map(count => Ok(JsNumber(count)))
  })

}
