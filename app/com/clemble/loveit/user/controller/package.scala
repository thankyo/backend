package com.clemble.loveit.user

import com.clemble.loveit.common.controller.jsonToWriteable
import com.clemble.loveit.user.model.User

package object controller {

  implicit val userWriteable = jsonToWriteable[User]

}
