package com.clemble.thank.controller

import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.PlaySpecification

trait ControllerSpec extends PlaySpecification {

  implicit val application = new GuiceApplicationBuilder().
    in(Mode.Test).
    build

}
