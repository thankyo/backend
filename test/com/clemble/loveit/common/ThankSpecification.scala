package com.clemble.loveit.common

import akka.stream.Materializer
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.PlaySpecification


import scala.reflect.ClassTag

trait ThankSpecification extends PlaySpecification {

  implicit lazy val application = ThankSpecification.application

  def dependency[T: ClassTag]: T = {
    application.injector.instanceOf[T]
  }

  import com.clemble.loveit.test.util._
  def someRandom[T](implicit generator: Generator[T]) = generator.generate()

  implicit lazy val materializer: Materializer = dependency[Materializer]

}

object ThankSpecification {

  lazy val application = new GuiceApplicationBuilder().
    in(Mode.Test).
    build

}
