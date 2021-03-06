package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.ChargeAccountService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
case class ChargeAccountController @Inject()(
                                              chargeAccService: ChargeAccountService,
                                              components: ControllerComponents,
                                              silhouette: Silhouette[AuthEnv]
                                            )(implicit ec: ExecutionContext
                                            ) extends LoveItController(silhouette, components) {

  def getMyAccount: Action[AnyContent] = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id
    chargeAccService.getChargeAccount(user).map(_ match {
      case Some(chAcc) => Ok(chAcc)
      case None => NoContent
    })
  })

  def setMyAccount: Action[String] = silhouette.SecuredAction.async(parse.json[String])(implicit req => {
    val user = req.identity.id
    val fUpdate = chargeAccService.updateChargeAccount(user, req.body)
    fUpdate.map(Ok(_))
  })

  def deleteMyAccount = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id
    val fDelete = chargeAccService.deleteChargeAccount(user)
    fDelete.map(_ => NoContent)
  })
}
