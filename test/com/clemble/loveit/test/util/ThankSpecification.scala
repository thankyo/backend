package com.clemble.loveit.test.util

import akka.stream.Materializer
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.PlaySpecification

trait ThankSpecification extends PlaySpecification {

  implicit lazy val application = ThankSpecification.application
  implicit lazy val materializer = application.injector.instanceOf[Materializer]

}

object ThankSpecification {

  lazy val application = new GuiceApplicationBuilder().
    in(Mode.Test).
    build

}
