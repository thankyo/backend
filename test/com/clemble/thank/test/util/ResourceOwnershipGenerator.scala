package com.clemble.thank.test.util

import com.clemble.thank.model.ResourceOwnership
import org.apache.commons.lang3.RandomStringUtils.randomNumeric
import org.apache.commons.lang3.RandomUtils._

object ResourceOwnershipGenerator extends Generator[ResourceOwnership] {

  override def generate(): ResourceOwnership = {
    if (nextInt(0, 2) == 0) {
      ResourceOwnership.full(ResourceGenerator.generate())
    } else if (nextInt(0, 1) == 0) {
      ResourceOwnership.partial(ResourceGenerator.generate())
    } else {
      ResourceOwnership.unrealized(ResourceGenerator.generate())
    }
  }

}
