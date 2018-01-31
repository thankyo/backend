package com.clemble.loveit.common

import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.user.model.User

trait ThankExecutor extends ThankSpecification {

  def createUser(profile: RegisterRequest = someRandom[RegisterRequest]): UserID

  def createProject(user: UserID = createUser(), resource: Resource = someRandom[Resource]): SupportedProject

  def getUser(user: UserID): Option[User]

}
