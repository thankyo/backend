package com.clemble.loveit.test.util

import com.clemble.loveit.thank.model.{OwnershipVerificationRequest, Pending}
import org.apache.commons.lang3.RandomStringUtils

object OwnershipVerificationGenerator extends Generator[OwnershipVerificationRequest] {
  override def generate(): OwnershipVerificationRequest = {
    val resource = ResourceGenerator.generate()
    val ownershipGenerator = ResourceOwnershipGenerator.generate()
    OwnershipVerificationRequest(
      Pending,
      resource,
      ownershipGenerator.ownershipType,
      RandomStringUtils.randomNumeric(10),
      RandomStringUtils.randomNumeric(10)
    )

  }
}
