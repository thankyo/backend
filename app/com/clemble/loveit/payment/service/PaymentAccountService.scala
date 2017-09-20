package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.PaymentException
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{ChargeAccount, PayoutAccount, StripeChargeAccount, StripeCustomerToken, StripePayoutAccount}
import com.clemble.loveit.payment.service.repository.PaymentAccountRepository
import com.google.common.collect.ImmutableMap
import com.stripe.Stripe
import com.stripe.model.Card
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.Results

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

/**
  * [[ChargeAccount]] integration service
  */
trait PaymentAccountService {

  def getChargeAccount(user: UserID): Future[Option[ChargeAccount]]

  def updateChargeAccount(user: UserID, token: StripeCustomerToken): Future[ChargeAccount]

  def getPayoutAccount(user: UserID): Future[Option[PayoutAccount]]

  def updatePayoutAccount(user: UserID, token: String): Future[PayoutAccount]

}

@Singleton
case class SimplePaymentAccountService @Inject()(repo: PaymentAccountRepository, chAccService: ChargeAccountConverter, implicit val ec: ExecutionContext) extends PaymentAccountService {


  override def getChargeAccount(user: UserID): Future[Option[ChargeAccount]] = {
    repo.getChargeAccount(user)
  }

  override def updateChargeAccount(user: UserID, token: StripeCustomerToken): Future[ChargeAccount] = {
    for {
      chAcc <- chAccService.processChargeToken(token)
      updated <- repo.setChargeAccount(user, chAcc)
    } yield {
      if (!updated) throw PaymentException.failedToLinkChargeAccount(user)
      chAcc
    }
  }

  override def getPayoutAccount(user: UserID): Future[Option[PayoutAccount]] = {
    repo.getPayoutAccount(user)
  }

  override def updatePayoutAccount(user: UserID, token: String): Future[PayoutAccount] = {
    for {
      ptAcc <- chAccService.processPayoutToken(token)
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
sealed trait ChargeAccountConverter {

  /**
    * Process payment request by the user
    */
  def processChargeToken(token: String): Future[_ <: ChargeAccount]

  /**
    * Converts payout token to [[PayoutAccount]]
    */
  def processPayoutToken(token: String): Future[_ <: PayoutAccount]

}


/**
  * Stripe processing service
  */
@Singleton
class StripeChargeAccountConverter(apiKey: String, clientKey: String, wsClient: WSClient, implicit val ec: ExecutionContext) extends ChargeAccountConverter {
  Stripe.apiKey = apiKey

  import com.stripe.model.Customer

  private def toInternalChargeAccount(customer: Customer): StripeChargeAccount = {
    val (brand, last4) = customer.
      getSources.
      getData().
      to[List].
      collectFirst({ case card: Card => (Option(card.getBrand), Option(card.getLast4))}).
      getOrElse(None -> None)
    StripeChargeAccount(customer.getId, brand, last4)
  }

  override def processChargeToken(token: String): Future[StripeChargeAccount] = {
    // Step 1. Generating customer params
    val customerParams = ImmutableMap.of[String, Object]("source", token)
    // Step 2. Generating customer
    val customer = Customer.create(customerParams)
    // Step 3. Converting to  Stripe customer with customer id
    val chargeAccount = toInternalChargeAccount(customer)
    // Step 4. Generating successful request
    Future.successful(chargeAccount)
  }

  /**
    * Converts payout token to [[PayoutAccount]]
    */
  override def processPayoutToken(token: String): Future[_ <: PayoutAccount] = {
    // Step 1. Make a request to PayoutToken
    val fRes = wsClient.url("https://connect.stripe.com/oauth/token").
      addQueryStringParameters(
        "client_secret" -> apiKey,
        "grant_type" -> "authorization_code",
        "client_id" -> clientKey,
        "code" -> token
      ).post(Json.obj())
    // Step 2. Convert response to PayoutAccount
    fRes.map(res => {
      val json = Json.parse(res.body)
      val accountId = (json \ "stripe_user_id").as[String]
      val refreshToken = (json \ "refresh_token").as[String]
      val accessToken = (json \ "access_token").as[String]
      StripePayoutAccount(accountId, refreshToken, accessToken)
    })
  }
}