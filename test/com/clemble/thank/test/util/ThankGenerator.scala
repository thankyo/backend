package com.clemble.thank.test.util

import com.clemble.thank.model.Thank
import org.apache.commons.lang3.RandomStringUtils._
import org.apache.commons.lang3.RandomUtils._

object ThankGenerator extends Generator[Thank] {

  override def generate(): Thank = Thank(random(10), nextLong(0, Long.MaxValue))

}
