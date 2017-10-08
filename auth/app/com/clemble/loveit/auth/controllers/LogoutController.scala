package com.clemble.loveit.auth.controllers

import javax.inject.Inject

import com.clemble.loveit.common.controller.CookieUtils
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{AbstractController, ControllerComponents}

class LogoutController @Inject() (
                                   silhouette: Silhouette[AuthEnv],
                                   components: ControllerComponents
                                 ) extends AbstractController(components) {

  def logout() = silhouette.UnsecuredAction(req => {
    Redirect("/").withCookies(CookieUtils.removeUser())
  })

}
