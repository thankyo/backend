package com.clemble.loveit.test.util

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.{ROVerificationRequest, Pending}
import org.apache.commons.lang3.RandomStringUtils

object ROVerificationGenerator extends Generator[ROVerificationRequest[Resource]] {

  override def generate(): ROVerificationRequest[Resource] = {
    val resource = ResourceGenerator.generate()
    val ownershipGenerator = ResourceOwnershipGenerator.generate()
    ROVerificationRequest(
      IDGenerator.generate(),
      Pending,
      resource,
      ownershipGenerator.ownershipType,
      RandomStringUtils.randomNumeric(10),
      RandomStringUtils.randomNumeric(10)
    )
  }

}
