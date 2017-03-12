package com.clemble.thank

import com.clemble.thank.model.User
import com.clemble.thank.social.auth.{SecureSocialRuntimeEnvironment, SimpleSecureSocialUserService}
import com.google.inject.{AbstractModule}
import net.codingwell.scalaguice.ScalaModule
import securesocial.core.services.{UserService => SecureSocialUserService}
import securesocial.core._

class SecureSocialModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[SecureSocialUserService[User]].to[SimpleSecureSocialUserService]
    bind[RuntimeEnvironment].to[SecureSocialRuntimeEnvironment]
  }

}
