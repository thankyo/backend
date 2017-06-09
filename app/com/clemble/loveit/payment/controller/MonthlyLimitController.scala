package com.clemble.loveit.payment.controller

import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.model.Money
import com.clemble.loveit.payment.service.repository.MonthlyLimitRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class MonthlyLimitController @Inject()(
                                        repo: MonthlyLimitRepository,
                                        silhouette: Silhouette[AuthEnv],
                                        implicit val ec: ExecutionContext
                                      ) extends Controller {

  def getMonthlyLimit = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id
    repo.getMonthlyLimit(user).map(_ match {
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
