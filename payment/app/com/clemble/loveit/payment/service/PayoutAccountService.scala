package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.PaymentException
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.PayoutAccount
import com.clemble.loveit.payment.service.repository.PayoutAccountRepository
import com.stripe.Stripe
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * [[PayoutAccount]] integration service
  */
trait PayoutAccountService {

  def getPayoutAccount(user: UserID): Future[Option[PayoutAccount]]

  def updatePayoutAccount(user: UserID, token: String): Future[PayoutAccount]

}

@Singleton
case class SimplePayoutAccountService @Inject()(repo: PayoutAccountRepository, converter: PayoutAccountConverter, implicit val ec: ExecutionContext) extends PayoutAccountService {

  override def getPayoutAccount(user: UserID): Future[Option[PayoutAccount]] = {
    repo.getPayoutAccount(user)
  }

  override def updatePayoutAccount(user: UserID, token: String): Future[PayoutAccount] = {
    for {
      ptAcc <- converter.processPayoutToken(token)
      updated <- repo.setPayoutAccount(user, ptAcc)
    } yield {
      if (!updated) throw PaymentException.failedToLinkChargeAccount(user)
      ptAcc
    }
  }

}


/**
  * Payment processing service abstraction
  */
sealed trait PayoutAccountConverter {

  /**
    * Converts payout token to [[PayoutAccount]]
    */
  def processPayoutToken(token: String): Future[_ <: PayoutAccount]

}

/**
  * Stripe processing service
  */
@Singleton
class StripePayoutAccountConverter @Inject() (wsClient: WSClient, implicit val ec: ExecutionContext) extends PayoutAccountConverter {

  /**
    * Converts payout token to [[PayoutAccount]]
    */
  override def processPayoutToken(token: String): Future[_ <: PayoutAccount] = {
    // Step 1. Make a request to PayoutToken
    val fRes = wsClient.url("https://connect.stripe.com/oauth/token").
      addQueryStringParameters(
        "client_secret" -> Stripe.apiKey,
        "grant_type" -> "authorization_code",
        "client_id" -> Stripe.clientId,
        "code" -> token
      ).post(Json.obj())
    // Step 2. Convert response to PayoutAccount
    fRes.map(res => {
      val json = Json.parse(res.body)
      val accountId = (json \ "stripe_user_id").as[String]
      val refreshToken = (json \ "refresh_token").as[String]
      val accessToken = (json \ "access_token").as[String]
      PayoutAccount(accountId, refreshToken, accessToken)
    })
  }
}