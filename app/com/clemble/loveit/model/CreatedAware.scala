package com.clemble.loveit.model

import org.joda.time.DateTime

trait CreatedAware {
  val created: DateTime
}
