package com.clemble.loveit.common

import com.clemble.loveit.thank.{DatabaseUpdater, SimpleDatabaseUpdater}
import javax.inject.Inject
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}

class MongoUpdateModule @Inject()(env: Environment, conf: Configuration) extends ScalaModule {

  override def configure(): Unit = {
    bind(classOf[DatabaseUpdater]).to(classOf[SimpleDatabaseUpdater]).asEagerSingleton()
  }

}
