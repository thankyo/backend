package com.clemble.loveit.test.util

import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.Thank
import org.apache.commons.lang3.RandomUtils._

object ThankGenerator extends Generator[Thank] {

  override def generate(): Thank = {
    Thank(
      ResourceGenerator.generate(),
      IDGenerator.generate(),
      nextLong(0, Long.MaxValue)
    )
  }

}
