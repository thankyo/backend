package com.clemble.loveit.payment.controller

import javax.inject.Inject

import com.clemble.loveit.common.controller.ControllerUtils
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.repository.UserBalanceRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.JsNumber
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class UserBalanceController @Inject()(
                                       repo: UserBalanceRepository,
                                       silhouette: Silhouette[AuthEnv],
                                       implicit val ec: ExecutionContext
                                     ) extends Controller {

  def getBalance(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    val userID = ControllerUtils.idOrMe(user)
    repo.getBalance(userID).map(balance => Ok(JsNumber(balance)))
  })

}
