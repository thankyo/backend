package com.clemble.loveit.test.util

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomStringUtils.random

object CommonSocialProfileGenerator extends Generator[CommonSocialProfile] {

  override def generate(): CommonSocialProfile = {
    CommonSocialProfile(
      loginInfo = LoginInfo("test", RandomStringUtils.random(10)),
      firstName = Some(random(10)),
      lastName = Some(random(10))
    )
  }

}
