package com.clemble.loveit.common.controller

import javax.inject.Singleton

import com.clemble.loveit.common.error.ThankException
import com.google.inject.{Inject, Provider}
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router
import play.api.{Configuration, Environment, OptionalSourceMapper}

import scala.concurrent._

@Singleton
class ErrorHandler @Inject() (
                               env: Environment,
                               config: Configuration,
                               sourceMapper: OptionalSourceMapper,
                               router: Provider[Router]
                             ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case te: ThankException => Future.successful(BadRequest(te))
      case exc => super.onServerError(request, exception)
    }
  }

}
