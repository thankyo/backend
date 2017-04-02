package com.clemble.loveit.common.model

import org.joda.time.DateTime

trait CreatedAware {
  val created: DateTime
}
