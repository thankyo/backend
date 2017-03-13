package com.clemble.thank.util

import com.clemble.thank.model.User
import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator

trait AuthEnv extends Env {
  type I = User
  type A = JWTAuthenticator
}
