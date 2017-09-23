package com.clemble.loveit.common.util

import com.clemble.loveit.user.model.{User}
import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator

/**
  * The auth env.
  */
trait AuthEnv extends Env {
  type I = User
  type A = JWTAuthenticator
}
