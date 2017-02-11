package com.clemble.thank.test.util

import com.clemble.thank.model.Thank
import org.apache.commons.lang3.RandomStringUtils._
import org.apache.commons.lang3.RandomUtils._

object ThankGenerator extends Generator[Thank] {

  override def generate(): Thank = {
    Thank(
      s"http://${randomAlphabetic(10)}.${randomAlphabetic(4)}/${randomAlphabetic(3)}/${randomAlphabetic(4)}",
      nextLong(0, Long.MaxValue)
    )
  }

}
