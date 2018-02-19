package com.clemble.loveit.dev

import com.clemble.loveit.dev.service.{DevInitializerService, SimpleDevInitializerService}
import net.codingwell.scalaguice.ScalaModule
import play.api
import play.api.{Configuration, Mode}

case class DevModule(env: api.Environment, conf: Configuration) extends ScalaModule {

  override def configure(): Unit = {
    if (env.mode == Mode.Dev) {
      bind[DevInitializerService].to[SimpleDevInitializerService].asEagerSingleton()
    }
  }

}
