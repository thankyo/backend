package com.clemble.thank.service

import org.specs2.mutable.Specification
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder

trait ServiceSpec extends Specification {

  val application = new GuiceApplicationBuilder().
    in(Mode.Test).
    build

}
