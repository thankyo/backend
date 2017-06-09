package com.clemble.loveit.payment.service

import javax.inject.Singleton

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{BankDetails, StripeBankDetails}
import com.google.common.collect.{ImmutableMap, Maps}
import com.stripe.model.Card

import scala.collection.JavaConversions._
import scala.concurrent.Future

trait BankDetailsService {

  def setChargeToken(user: UserID, token: String): Future[BankDetails]

  def setPayoutToken(user: UserID, token: String): Future[BankDetails]

}

/**
  * Payment processing service abstraction
  */
sealed trait ChargeBankDetailsService {

  /**
    * Process payment request by the user
    */
  def process(token: String): Future[_ <: BankDetails]

}


/**
  * Stripe processing service
  */
@Singleton
case object StripeChargeBankDetailsService extends ChargeBankDetailsService {
  import com.stripe.model.Customer

  private def toInternalBankDetails(customer: Customer): StripeBankDetails = {
    val (brand, last4) = customer.
      getSources.
      getData().
      to[List].
      collectFirst({ case card: Card => (Option(card.getBrand), Option(card.getLast4))}).
      getOrElse(None -> None)
    StripeBankDetails(customer.getId, brand, last4)
  }

  override def process(token: String): Future[StripeBankDetails] = {
    // Step 1. Generating customer params
    val customerParams = ImmutableMap.of[String, Object]("source", token)
    // Step 2. Generating customer
    val customer = Customer.create(customerParams)
    // Step 3. Converting to  Stripe customer with customer id
    val bankDetails = toInternalBankDetails(customer)
    // Step 4. Generating successful request
    Future.successful(bankDetails)
  }

}