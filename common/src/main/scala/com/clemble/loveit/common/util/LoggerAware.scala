package com.clemble.loveit.common.util

import play.api.Logger

trait LoggerAware {

  val LOG = Logger(this.getClass())

}
