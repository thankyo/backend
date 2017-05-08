package com.clemble.loveit.common

import akka.stream.Materializer
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.PlaySpecification

import scala.reflect.ClassTag

trait ThankSpecification extends PlaySpecification {

  implicit lazy val application = ThankSpecification.application
  implicit lazy val materializer = application.injector.instanceOf[Materializer]

  def instanceOf[T: ClassTag]: T = {
    application.injector.instanceOf[T]
  }

}

object ThankSpecification {

  lazy val application = new GuiceApplicationBuilder().
    in(Mode.Test).
    build

}
