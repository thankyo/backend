package com.clemble.loveit.payment.controller

import com.clemble.loveit.common.util.AuthEnv
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.payment.service.ChargeAccountService
import com.clemble.loveit.payment.service.repository.EOMChargeRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class EOMChargeController @Inject()(
                                          chargeRepo: EOMChargeRepository,
                                          chAccService: ChargeAccountService,
                                          silhouette: Silhouette[AuthEnv],
                                          implicit val ec: ExecutionContext
) extends Controller {

  def listMy() = silhouette.SecuredAction(req => {
    val charges = chargeRepo.findByUser(req.identity.id)
    Ok.chunked(charges)
  })

  def getMyAccount = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id
    chAccService.getChargeAccount(user).map(_ match {
      case Some(chAcc) => Ok(chAcc)
      case None => NotFound
    })
  })

  def setMyAccount = silhouette.SecuredAction.async(parse.json[String])(implicit req => {
    val user = req.identity.id
    val fUpdate = chAccService.updateChargeAccount(user, req.body)
    fUpdate.map(Ok(_))
  })

}
