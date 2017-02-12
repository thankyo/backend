package com.clemble.thank.service

import org.specs2.mutable.Specification
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.PlaySpecification

trait ServiceSpec extends PlaySpecification {

  val application = new GuiceApplicationBuilder().
    in(Mode.Test).
    build

}
