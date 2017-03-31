package com.clemble.loveit.test.util

import com.clemble.loveit.common.error.ThankException

import scala.util.Random

object ThankExceptionGenerator extends Generator[ThankException] {

  override def generate(): ThankException = {
    if (Random.nextBoolean())
      RepositoryExceptionGenerator.generate()
    else
      UserExceptionGenerator.generate()
  }

}
