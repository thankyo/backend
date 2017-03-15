package com.clemble.thank.test.util

import com.clemble.thank.model._
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.apache.commons.lang3.RandomUtils.nextInt

object ResourceGenerator extends Generator[Resource]{
  override def generate(): Resource = {
    if (nextInt(0, 1) == 0)
      HttpResource(s"${randomAlphabetic(10)}.${randomAlphabetic(4)}/${randomAlphabetic(3)}/${randomAlphabetic(4)}")
    else
      FacebookResource(RandomStringUtils.randomNumeric(30))
  }
}
