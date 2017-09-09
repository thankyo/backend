package com.clemble.loveit.payment.controller
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.CookieUtils
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.PaymentAccountService
import com.clemble.loveit.payment.service.repository.EOMPayoutRepository
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.crypto.Crypter
import play.api.Configuration
import play.api.mvc.{Controller, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class EOMPayoutController @Inject()(
                                          conf: Configuration,
                                          crypter: Crypter,
                                          payoutRepo: EOMPayoutRepository,
                                          paymentAccService: PaymentAccountService,
                                          silhouette: Silhouette[AuthEnv],
                                          implicit val ec: ExecutionContext
                                   ) extends Controller {

  private val clientId = conf.getString("payment.stripe.clientId").get

  private def toStripeUrl(user: UserID) = {
    s"https://connect.stripe.com/oauth/authorize?" +
      s"response_type=code&" +
      s"client_id=${clientId}&" +
      s"scope=read_write&" +
      s"state=${user}"
  }

  private def readUser(encUser: UserID) = {
    encUser
  }


  private def doConnectMyAccount[A](user: UserID, req: Request[A]): Future[Result] = {
    if (req.queryString.isEmpty) {
      CookieUtils.readUser(req)
      Future.successful(Redirect(toStripeUrl(user)))
    } else {
      val tokenOpt = req.queryString.get("code").flatMap(_.headOption)
      val userOpt = req.queryString.get("state").flatMap(_.headOption).map(readUser)
      (tokenOpt, userOpt) match {
        case (Some(token), Some(user)) =>
          paymentAccService.
            updatePayoutAccount(user, token).
            map(_ => Redirect("/creator/my"))
        case _ =>
          Future.successful(BadRequest)
      }
    }
  }

  def listMy() = silhouette.SecuredAction(req => {
    val payouts = payoutRepo.findByUser(req.identity.id)
    Ok.chunked(payouts)
  })

  def connectMyAccount = silhouette.SecuredAction.async(implicit req => {
    val userOpt = CookieUtils.readUser(req)
    if (userOpt.isEmpty) {
      Future.successful(BadRequest("No user exists"))
    } else {
      val user = userOpt.get
      doConnectMyAccount(user, req)
    }
  })

}
