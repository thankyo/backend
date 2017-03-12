package com.clemble.thank.social.auth

import com.clemble.thank.model.User
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.MessagesApi
import securesocial.core.providers.FacebookProvider
import securesocial.core.services.{UserService => SecureSocialUserService}
import securesocial.core.RuntimeEnvironment

import scala.collection.immutable.ListMap


@Singleton
class SecureSocialRuntimeEnvironment @Inject()(
                                                override val configuration: Configuration,
                                                override val messagesApi: MessagesApi,
                                                override val userService: SecureSocialUserService[User]
                                              ) extends RuntimeEnvironment.Default {
  override type U = User
  override lazy val providers = ListMap(
    include(new FacebookProvider(routes, cacheService, oauth2ClientFor(FacebookProvider.Facebook)))
  )
}