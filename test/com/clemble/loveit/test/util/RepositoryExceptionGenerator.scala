package com.clemble.loveit.test.util

import com.clemble.loveit.common.error.RepositoryException

object RepositoryExceptionGenerator extends Generator[RepositoryException] {

  override def generate(): RepositoryException = {
    new RepositoryException(RepositoryErrorGenerator.generate())
  }

}
