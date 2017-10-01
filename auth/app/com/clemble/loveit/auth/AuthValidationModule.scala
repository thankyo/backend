package com.clemble.loveit.auth

import com.clemble.loveit.auth.models.daos.{AuthTokenDAO, AuthTokenDAOImpl}
import com.clemble.loveit.auth.models.services.{AuthTokenService, AuthTokenServiceImpl}
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
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
  }
}
