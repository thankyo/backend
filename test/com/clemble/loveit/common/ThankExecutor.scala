package com.clemble.loveit.common

import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.common.model.{Project, Resource, UserID}
import com.clemble.loveit.common.model.User

trait ThankExecutor extends ThankSpecification {

  def createUser(profile: RegistrationRequest = someRandom[RegistrationRequest]): UserID

  def createProject(user: UserID = createUser(), url: Resource = randomResource): Project

  def getUser(user: UserID): Option[User]

}
