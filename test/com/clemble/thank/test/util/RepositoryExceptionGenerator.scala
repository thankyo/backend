package com.clemble.thank.test.util

import com.clemble.thank.model.error.RepositoryException

object RepositoryExceptionGenerator extends Generator[RepositoryException] {

  override def generate(): RepositoryException = {
    new RepositoryException(RepositoryErrorGenerator.generate())
  }

}
