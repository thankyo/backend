package com.clemble.thank.test.util

import com.clemble.thank.model.error.ThankException

import scala.util.Random

object ThankExceptionGenerator extends Generator[ThankException] {

  override def generate(): ThankException = {
    if (Random.nextBoolean())
      RepositoryExceptionGenerator.generate()
    else
      UserExceptionGenerator.generate()
  }

}
