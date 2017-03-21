package com.clemble.thank.test.util

import com.clemble.thank.model.error.UserException

object UserExceptionGenerator extends Generator[UserException]{

  override def generate(): UserException = UserException.notEnoughFunds()

}
