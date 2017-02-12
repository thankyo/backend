package com.clemble.thank.test.util

import com.clemble.thank.model.error.ThankException

object ThankExceptionGenerator extends Generator[ThankException] {

  override def generate(): ThankException = RepositoryExceptionGenerator.generate()

}
