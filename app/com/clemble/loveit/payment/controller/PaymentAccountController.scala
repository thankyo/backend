package com.clemble.loveit.payment.controller

import com.clemble.loveit.common.util.AuthEnv
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.payment.service.PaymentAccountService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class PaymentAccountController @Inject()(
                                              paymentAccService: PaymentAccountService,
                                              silhouette: Silhouette[AuthEnv],
                                              implicit val ec: ExecutionContext
) extends Controller {

  def getMyAccount = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id
    paymentAccService.getChargeAccount(user).map(_ match {
      case Some(chAcc) => Ok(chAcc)
      case None => Ok(Json.obj("notConnected" -> true))
    })
  })

  def setMyAccount = silhouette.SecuredAction.async(parse.json[String])(implicit req => {
    val user = req.identity.id
    val fUpdate = paymentAccService.updateChargeAccount(user, req.body)
    fUpdate.map(Ok(_))
  })

}
