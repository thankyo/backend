package com.clemble.loveit.payment.service

import java.util
import javax.inject.Singleton

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{BankDetails, StripeBankDetails}
import com.google.common.collect.{ImmutableMap, Maps}

import scala.concurrent.Future

trait BankDetailsService {

  def setChargeToken(user: UserID, token: String): Future[BankDetails]

  def setPayoutToken(user: UserID, token: String): Future[BankDetails]

}

/**
  * Payment processing service abstraction
  */
sealed trait ChargeBankDetailsService[T <: BankDetails] {

  /**
    * Process payment request by the user
    */
  def process(token: String): Future[T]

}

import com.stripe.model.Customer

/**
  * Stripe processing service
  */
@Singleton
case object StripeProcessingService extends ChargeBankDetailsService[StripeBankDetails] {

  override def process(token: String): Future[StripeBankDetails] = {
    // Step 1. Generating customer params
    val customerParams = ImmutableMap.of[String, Object]("source", token)
    // Step 2. Generating customer
    val customer = Customer.create(customerParams)
    // Step 3. Converting to  Stripe customer with customer id
    val bankDetails = StripeBankDetails(customer.getId())
    // Step 4. Generating successful request
    Future.successful(bankDetails)
  }

}