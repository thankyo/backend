package com.clemble.loveit.payment.controller

import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

/**
  * Created by mavarazy on 5/2/17.
  */
class StripeController(
                        silhouette: Silhouette[AuthEnv],
                        implicit val ec: ExecutionContext
                      ) extends Controller {

}
