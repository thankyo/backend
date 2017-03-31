package com.clemble.loveit.test.util

import com.clemble.loveit.model.Thank
import org.apache.commons.lang3.RandomStringUtils._
import org.apache.commons.lang3.RandomUtils._

object ThankGenerator extends Generator[Thank] {

  override def generate(): Thank = {
    Thank(
      ResourceGenerator.generate(),
      nextLong(0, Long.MaxValue)
    )
  }

}
