package com.clemble.loveit.thank.model

import com.clemble.loveit.user.model.{User}

trait UserSupported {

  val supported: List[User]

}
