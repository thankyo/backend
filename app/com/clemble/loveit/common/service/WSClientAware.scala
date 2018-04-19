package com.clemble.loveit.common.service

import play.api.libs.ws.WSClient

trait WSClientAware {

  val client: WSClient

}
