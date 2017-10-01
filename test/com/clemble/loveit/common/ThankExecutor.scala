package com.clemble.loveit.common

import com.clemble.loveit.auth.models.requests.SignUpRequest
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.user.model.User

trait ThankExecutor extends ThankSpecification {

  def createUser(profile: SignUpRequest = someRandom[SignUpRequest]): UserID

  def getUser(user: UserID): Option[User]

}
