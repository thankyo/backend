package com.clemble.loveit.common

import com.clemble.loveit.common.model.Amount
import com.clemble.loveit.test.util.CommonSocialProfileGenerator
import com.clemble.loveit.user.controller.SocialAuthController
import com.clemble.loveit.user.service.repository.UserRepository
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import play.api.test.FakeRequest

trait ServiceSpec extends ThankSpecification {

  lazy val authController = dependency[SocialAuthController]
  lazy val userRep = dependency[UserRepository]

  def createUser(socialProfile: CommonSocialProfile = CommonSocialProfileGenerator.generate(), balance: Amount = 200): Seq[(String, String)] = {
    val userIdentity = await(authController.createOrUpdateUser(socialProfile)(FakeRequest()))
    await(userRep.changeBalance(userIdentity.id, balance))
    Seq("id" -> userIdentity.id)
  }

}
