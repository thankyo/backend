package com.clemble.thank.test.util

import com.clemble.thank.model.{ResourceOwnership, User}
import org.apache.commons.lang3.RandomStringUtils.random
import org.apache.commons.lang3.RandomUtils.nextLong
import org.joda.time.DateTime
import securesocial.core.BasicProfile

object UserGenerator extends Generator[User] {

  override def generate(): User = {
    User(
      id = random(10),
      firstName = Some(random(10)),
      lastName = Some(random(10)),
      owns = List.empty,
      balance = 0L,
      bankDetails = BankDetailsGenerator.generate(),
      thumbnail = Some(random(12)),
      dateOfBirth = Some(new DateTime(nextLong(0, Long.MaxValue))),
      profiles = List.empty[BasicProfile]
    )
  }

  def generate(ownership: ResourceOwnership): User = {
    generate().copy(owns = List(ownership))
  }

}
