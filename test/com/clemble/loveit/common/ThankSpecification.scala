package com.clemble.loveit.common

import play.api.test.PlaySpecification

trait ThankSpecification extends PlaySpecification {

  import com.clemble.loveit.test.util._
  def someRandom[T](implicit generator: Generator[T]) = generator.generate()

}