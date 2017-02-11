package com.clemble.thank.test.util

import com.clemble.thank.model.error.RepositoryError
import org.apache.commons.lang3.RandomStringUtils._

object RepositoryErrorGenerator extends Generator[RepositoryError] {
  override def generate(): RepositoryError = RepositoryError(random(10), random(20))
}
