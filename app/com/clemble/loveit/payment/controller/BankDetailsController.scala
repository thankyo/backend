package com.clemble.loveit.payment.controller

import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.BankDetailsService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext


case class BankDetailsController @Inject()(
                                       bankDetailsService: BankDetailsService,
                                       silhouette: Silhouette[AuthEnv],
                                       implicit val ec: ExecutionContext
                                     ) extends Controller {

  def getMyBankDetails = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id
    bankDetailsService.getBankDetails(user).map(_ match {
      case Some(bankDetails) => Ok(bankDetails)
      case None => NotFound
    })
  })

  def updateMyBankDetails = silhouette.SecuredAction.async(parse.json[String])(implicit req => {
    val user = req.identity.id
    val fUpdate = bankDetailsService.updateBankDetails(user, req.body)
    fUpdate.map(Ok(_))
  })

}