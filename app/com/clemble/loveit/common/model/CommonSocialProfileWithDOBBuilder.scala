package com.clemble.loveit.common.model

import com.mohiva.play.silhouette.impl.providers.SocialProfileBuilder

/**
  * The profile builder for the common social profile.
  */
trait CommonSocialProfileWithDOBBuilder {
  self: SocialProfileBuilder =>

  /**
    * The type of the profile a profile builder is responsible for.
    */
  type Profile = CommonSocialProfileWithDOB
}
