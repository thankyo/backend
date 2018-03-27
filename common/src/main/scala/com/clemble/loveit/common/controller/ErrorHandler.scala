package com.clemble.loveit.common.controller

import javax.inject.{Inject, Provider, Singleton}
import com.clemble.loveit.common.error.{FieldValidationError, ResourceException, SelfLovingForbiddenException}
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException, UnexpectedResponseException}
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
      case fv: FieldValidationError =>
        Future.successful(BadRequest(fv))
      case _: InvalidPasswordException =>
        Future.successful(BadRequest(FieldValidationError("email", "Email or Password does not match")))
      case _: IdentityNotFoundException =>
        Future.successful(BadRequest(FieldValidationError("email", "This email was not registered")))
      case ure : UnexpectedResponseException if (ure.getMessage.contains("This authorization code has expired."))=>
        Future.successful(BadRequest(FieldValidationError("authCode", "This authorization code has expired.")))
      case re: ResourceException =>
        Future.successful(BadRequest(FieldValidationError("error", re.message)))
      case sle: SelfLovingForbiddenException =>
        Future.successful(BadRequest(sle.message))
      case pe: ProviderException =>
        Future.successful(InternalServerError(pe.getMessage))
      case exc =>
        super.onServerError(request, exc)
    }
  }

}
