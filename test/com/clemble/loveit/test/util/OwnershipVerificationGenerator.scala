package com.clemble.loveit.test.util

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.{OwnershipVerificationRequest, Pending}
import org.apache.commons.lang3.RandomStringUtils

object OwnershipVerificationGenerator extends Generator[OwnershipVerificationRequest[Resource]] {
  override def generate(): OwnershipVerificationRequest[Resource] = {
    val resource = ResourceGenerator.generate()
    val ownershipGenerator = ResourceOwnershipGenerator.generate()
    OwnershipVerificationRequest(
      IDGenerator.generate(),
      Pending,
      resource,
      ownershipGenerator.ownershipType,
      RandomStringUtils.randomNumeric(10),
      RandomStringUtils.randomNumeric(10)
    )

  }
}
