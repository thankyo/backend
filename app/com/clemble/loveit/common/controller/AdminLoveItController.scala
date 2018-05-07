package com.clemble.loveit.common.controller

import com.clemble.loveit.auth.AdminAuthEnv
import com.mohiva.play.silhouette.api.Silhouette
import org.slf4j.LoggerFactory
import play.api.mvc.{AbstractController, ControllerComponents}

abstract class AdminLoveItController(silhouette: Silhouette[AdminAuthEnv], controllerComponents: ControllerComponents) extends AbstractController(controllerComponents) {

  val LOG = LoggerFactory.getLogger(getClass())

}
