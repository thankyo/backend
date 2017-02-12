package com.clemble.thank.test.util

import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.PlaySpecification

trait ThankSpecification extends PlaySpecification {

  implicit lazy val application = ThankSpecification.application

}

object ThankSpecification {

  lazy val application = new GuiceApplicationBuilder().
    in(Mode.Test).
    build

}
