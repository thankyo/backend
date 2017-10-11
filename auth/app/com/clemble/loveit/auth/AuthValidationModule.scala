package com.clemble.loveit.auth

import com.clemble.loveit.auth.service.repository.AuthTokenRepository
import com.clemble.loveit.auth.service.repository.mongo.MongoAuthTokenRepository
import com.clemble.loveit.auth.service.{AuthTokenService, SimpleAuthTokenService}
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule

/**
 * The base Guice module.
 */
class AuthValidationModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure(): Unit = {
    bind[AuthTokenRepository].to[MongoAuthTokenRepository]
    bind[AuthTokenService].to[SimpleAuthTokenService]
  }
}
