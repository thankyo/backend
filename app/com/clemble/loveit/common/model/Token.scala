package com.clemble.loveit.common.model

import java.util.UUID

trait Token extends UserAware with CreatedAware {
  val token: UUID
}
