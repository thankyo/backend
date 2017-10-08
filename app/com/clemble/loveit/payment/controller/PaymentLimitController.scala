package com.clemble.loveit.payment.controller

import javax.inject.Inject

import com.clemble.loveit.common.controller.ControllerUtils
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.model.Money
import com.clemble.loveit.payment.service.repository.PaymentLimitRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class PaymentLimitController @Inject()(
                                        repo: PaymentLimitRepository,
                                        components: ControllerComponents,
                                        silhouette: Silhouette[AuthEnv],
                                        implicit val ec: ExecutionContext
                                      ) extends AbstractController(components) {

  def getMonthlyLimit(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    val userID = ControllerUtils.idOrMe(user)
    repo.getMonthlyLimit(userID).map(_ match {
      case Some(limit) => Ok(limit)
      case None => NotFound
    })
  })

  def setMonthlyLimit = silhouette.SecuredAction.async(parse.json[Money])(implicit req => {
    val user = req.identity.id
    val fLimit = repo.setMonthlyLimit(user, req.body)
    fLimit.map(_ match {
      case true => Ok(req.body)
      case false => InternalServerError("Failed to update user")
    })
  })

}
