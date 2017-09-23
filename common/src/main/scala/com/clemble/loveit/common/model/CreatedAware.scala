package com.clemble.loveit.common.model

import java.time.LocalDateTime

trait CreatedAware {
  val created: LocalDateTime
}
