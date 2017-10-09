package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Named, Singleton}

import com.clemble.loveit.common.controller.CookieUtils
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.{ChargeAccountService, PayoutAccountService}
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.crypto.Crypter
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PayoutAccountController @Inject()(
                                         conf: Configuration,
                                         @Named("paymentCrypter") crypter: Crypter,
                                         payoutAccService: PayoutAccountService,
                                         components: ControllerComponents,
                                         silhouette: Silhouette[AuthEnv],
                                         implicit val ec: ExecutionContext
                                   ) extends AbstractController(components) {

  private val clientId = conf.get[String]("payment.stripe.clientId")

  private def toStripeUrl(user: UserID) = {
    s"https://connect.stripe.com/oauth/authorize?" +
      s"response_type=code&" +
      s"client_id=${clientId}&" +
      s"scope=read_write&" +
      s"state=${crypter.encrypt(user)}"
  }

  private def doConnectMyAccount[A](user: UserID, req: Request[A]): Future[Result] = {
    if (req.queryString.isEmpty) {
      CookieUtils.readUser(req)
      Future.successful(Redirect(toStripeUrl(user)))
    } else {
      val tokenOpt = req.queryString.get("code").flatMap(_.headOption)
      val userOpt = req.queryString.get("state").flatMap(_.headOption).map(crypter.decrypt)
      (tokenOpt, userOpt) match {
        case (Some(token), Some(user)) =>
          payoutAccService.
            updatePayoutAccount(user, token).
            map(_ => Redirect("/creator/my"))
        case _ =>
          Future.successful(BadRequest)
      }
    }
  }

  def connectMyAccount = silhouette.UnsecuredAction.async(implicit req => {
    val userOpt = CookieUtils.readUser(req)
    if (userOpt.isEmpty) {
      Future.successful(BadRequest("No user exists"))
    } else {
      val user = userOpt.get
      doConnectMyAccount(user, req)
    }
  })

}
