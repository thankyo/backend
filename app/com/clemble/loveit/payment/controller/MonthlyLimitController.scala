package com.clemble.loveit.payment.controller

import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.BankDetailsService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class MonthlyLimitController @Inject()(
                                        repo: BankDetailsService,
                                        silhouette: Silhouette[AuthEnv],
                                        implicit val ec: ExecutionContext
                                      ) extends Controller {

}
