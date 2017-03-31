package com.clemble.loveit.test.util

import com.clemble.loveit.model.error.UserException

object UserExceptionGenerator extends Generator[UserException]{

  override def generate(): UserException = UserException.notEnoughFunds()

}
