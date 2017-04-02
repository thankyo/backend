package com.clemble.loveit.common.util

import com.clemble.loveit.user.model.{UserIdentity}
import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator

trait AuthEnv extends Env {
  type I = UserIdentity
  type A = JWTAuthenticator
}
