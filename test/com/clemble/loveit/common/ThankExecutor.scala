package com.clemble.loveit.common

import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.user.model.User

trait ThankExecutor extends ThankSpecification {

  def createUser(profile: RegisterRequest = someRandom[RegisterRequest]): UserID

  def getUser(user: UserID): Option[User]

}
