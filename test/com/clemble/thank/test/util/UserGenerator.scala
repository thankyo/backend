package com.clemble.thank.test.util

import com.clemble.thank.model.{PayPalBankDetails, User}
import org.apache.commons.lang3.RandomStringUtils.random
import org.apache.commons.lang3.RandomUtils.nextLong
import org.joda.time.DateTime

object UserGenerator extends Generator[User] {

  override def generate(): User = {
    User(
      random(10),
      random(10),
      random(10),
      random(10),
      List.empty,
      nextLong(0, Long.MaxValue),
      PayPalBankDetails(random(12)),
      Some(random(12)),
      new DateTime(nextLong(0, Long.MaxValue))
    )
  }

}
