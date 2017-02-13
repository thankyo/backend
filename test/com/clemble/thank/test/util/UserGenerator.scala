package com.clemble.thank.test.util

import com.clemble.thank.model.{ResourceOwnership, User}
import org.apache.commons.lang3.RandomStringUtils.random
import org.apache.commons.lang3.RandomUtils.nextLong
import org.joda.time.DateTime

object UserGenerator extends Generator[User] {

  override def generate(): User = {
    User(
      random(10),
      random(10),
      random(10),
      List.empty,
      0,
      BankDetailsGenerator.generate(),
      Some(random(12)),
      new DateTime(nextLong(0, Long.MaxValue))
    )
  }

  def generate(ownership: ResourceOwnership): User = {
    generate().copy(owns = List(ownership))
  }

}
