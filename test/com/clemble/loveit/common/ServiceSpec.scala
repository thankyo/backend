package com.clemble.loveit.common

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.service.ROService
import com.clemble.loveit.user.service.UserService
import com.clemble.loveit.user.service.repository.UserRepository
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import controllers.SocialAuthController

trait ServiceSpec extends ThankSpecification {

  lazy val authController = dependency[SocialAuthController]
  lazy val userService = dependency[UserService]
  lazy val userRep = dependency[UserRepository]

  lazy val resService = dependency[ROService]

  def someUser(socialProfile: CommonSocialProfile = someRandom[CommonSocialProfile]): User = {
    await(userService.createOrUpdateUser(socialProfile)) match {
      case Left(user) => user
      case Right(user) => user
    }
  }

  def createUser(socialProfile: CommonSocialProfile = someRandom[CommonSocialProfile]): UserID = {
    val userIdentity = await(userService.createOrUpdateUser(socialProfile))
    userIdentity match {
      case Left(user) => user.id
      case Right(user) => user.id
    }
  }

  def assignOwnership(user: UserID, res: Resource) = await(resService.assignOwnership(user, res))
}
