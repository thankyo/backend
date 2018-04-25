package com.clemble.loveit.common.model

import java.util.UUID

trait TokenAware extends UserAware with CreatedAware {
  val token: UUID
}
