package com.clemble.thank.controller

import play.api.http.{DefaultHttpErrorHandler, HttpErrorConfig, HttpErrorHandler}
import play.api.mvc._

import scala.concurrent._
import javax.inject.Singleton

import com.clemble.thank.controller.ControllerSafeUtils.thankExceptionWriteable
import play.api.mvc.Results._
import com.clemble.thank.model.error.ThankException
import com.google.inject.{Inject, Provider}
import play.api.{Configuration, Environment, OptionalSourceMapper}
import play.api.routing.Router

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