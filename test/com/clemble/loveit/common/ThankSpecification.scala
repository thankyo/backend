package com.clemble.loveit.common

import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import play.api.test.PlaySpecification

trait ThankSpecification extends PlaySpecification {

  import com.clemble.loveit.test.util._
  def someRandom[T](implicit generator: Generator[T]) = generator.generate()

  def randomResource = s"http://${randomAlphabetic(10)}.${randomAlphabetic(4)}/${randomAlphabetic(3)}/${randomAlphabetic(4)}"

}