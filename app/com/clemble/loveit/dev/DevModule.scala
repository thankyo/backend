package com.clemble.loveit.dev

import com.clemble.loveit.dev.service.{DevInitializerService, SimpleDevInitializerService}
import net.codingwell.scalaguice.ScalaModule
import play.api
import play.api.Configuration

case class DevModule(env: api.Environment, conf: Configuration) extends ScalaModule {

  override def configure(): Unit = {
    bind[DevInitializerService].to[SimpleDevInitializerService].asEagerSingleton()
  }

}
