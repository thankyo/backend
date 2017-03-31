package com.clemble.loveit.test.util

import com.clemble.loveit.model.error.RepositoryException

object RepositoryExceptionGenerator extends Generator[RepositoryException] {

  override def generate(): RepositoryException = {
    new RepositoryException(RepositoryErrorGenerator.generate())
  }

}
