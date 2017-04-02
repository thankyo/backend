package com.clemble.loveit.thank

import com.clemble.loveit.common.controller.jsonToWriteable
import com.clemble.loveit.thank.model.{ResourceOwnership, Thank}

package object controller {

  implicit val thankWriteable = jsonToWriteable[Thank]
  implicit val resourceOwnershipWriteable = jsonToWriteable[ResourceOwnership]

}
