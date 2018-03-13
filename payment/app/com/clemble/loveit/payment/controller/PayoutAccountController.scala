package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Named, Singleton}

import com.clemble.loveit.common.controller.{CookieUtils, LoveItController}
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.PayoutAccountService
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.crypto.{Base64, Crypter}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PayoutAccountController @Inject()(
  conf: Configuration,
  @Named("paymentCrypter") crypter: Crypter,
  payoutAccService: PayoutAccountService,
  components: ControllerComponents,
  silhouette: Silhouette[AuthEnv])
  (
    implicit val ec: ExecutionContext,
    implicit val cookieUtils: CookieUtils
  ) extends LoveItController(components) {

  private val clientId = conf.get[String]("payment.stripe.clientId")

  private def toStripeUrl(user: UserID) = {
    s"https://connect.stripe.com/oauth/authorize?" +
      s"response_type=code&" +
      s"client_id=${clientId}&" +
      s"scope=read_write&" +
      s"type=standard&" +
      s"state=${Base64.encode(crypter.encrypt(user))}"
  }

  def getMyAccount = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id
    payoutAccService.getPayoutAccount(user).
      map({
        case Some(account) => Ok(Json.obj("account" -> account.accountId))
        case _ => NoContent
      })
  })

  def connectMyAccount = silhouette.UnsecuredAction.async(implicit req => {
    val userOpt = cookieUtils.readUser(req)
    if (userOpt.isEmpty) {
      throw new IllegalArgumentException("No user exists")
    }
    val user = userOpt.get
    if (req.queryString.isEmpty) {
      Future.successful(Redirect(toStripeUrl(user)))
    } else {
      val tokenOpt = req.queryString.get("code").flatMap(_.headOption)
      val expectedUser = cookieUtils.readUser(req)
      val userOpt = req.queryString.get("state").flatMap(_.headOption).map(Base64.decode).map(crypter.decrypt)
      if (userOpt != expectedUser) {
        Future.successful(BadRequest)
      } else {
        (tokenOpt, userOpt) match {
          case (Some(token), Some(user)) =>
            payoutAccService.
              updatePayoutAccount(user, token).
              map(_ => Redirect("/settings/payout"))
          case _ =>
            Future.successful(BadRequest)
        }
      }
    }
  })

  def deleteMyAccount() = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id
    val fDelete = payoutAccService.deletePayoutAccount(user)
    fDelete.map(_ => NoContent)
  })

}
