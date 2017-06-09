package com.clemble.loveit.common

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.service.ResourceOwnershipService
import com.clemble.loveit.user.controller.SocialAuthController
import com.clemble.loveit.user.model.UserIdentity
import com.clemble.loveit.user.service.repository.UserRepository
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import play.api.test.FakeRequest

trait ServiceSpec extends ThankSpecification {

  lazy val authController = dependency[SocialAuthController]
  lazy val userRep = dependency[UserRepository]

  lazy val resService = dependency[ResourceOwnershipService]

  def someUser(socialProfile: CommonSocialProfile = someRandom[CommonSocialProfile]): UserIdentity = {
    await(authController.createOrUpdateUser(socialProfile)(FakeRequest()))
  }

  // TODO remove balance it's no longer relevant
  def createUser(socialProfile: CommonSocialProfile = someRandom[CommonSocialProfile]): String = {
    val userIdentity = await(authController.createOrUpdateUser(socialProfile)(FakeRequest()))
    userIdentity.id
  }

  def assignOwnership(user: UserID, res: Resource) = await(resService.assignOwnership(user, res))
}
