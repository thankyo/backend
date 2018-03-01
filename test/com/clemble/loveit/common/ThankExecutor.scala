package com.clemble.loveit.common

import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.user.model.User

trait ThankExecutor extends ThankSpecification {

  def createUser(profile: RegistrationRequest = someRandom[RegistrationRequest]): UserID

  def createProject(user: UserID = createUser(), resource: Resource = randomResource): Project

  def getUser(user: UserID): Option[User]

}
