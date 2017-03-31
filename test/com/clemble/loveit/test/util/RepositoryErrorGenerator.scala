package com.clemble.loveit.test.util

import com.clemble.loveit.common.error.RepositoryError
import org.apache.commons.lang3.RandomStringUtils._

object RepositoryErrorGenerator extends Generator[RepositoryError] {
  override def generate(): RepositoryError = RepositoryError(random(10), random(20))
}
