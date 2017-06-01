package com.clemble.loveit.test.util

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.{ROVerification, Pending}
import org.apache.commons.lang3.RandomStringUtils

object ROVerificationGenerator extends Generator[ROVerification[Resource]] {

  override def generate(): ROVerification[Resource] = {
    val resource = ResourceGenerator.generate()
    ROVerification(
      IDGenerator.generate(),
      Pending,
      resource,
      RandomStringUtils.randomNumeric(10),
      RandomStringUtils.randomNumeric(10)
    )
  }

}
